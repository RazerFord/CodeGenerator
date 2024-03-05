package org.codegenerator.generator.methodsequencefinders.concrete;

import kotlin.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.codegenerator.Utils;
import org.codegenerator.exceptions.InvariantCheckingException;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.graph.LazyGraph;
import org.codegenerator.generator.graph.Path;
import org.codegenerator.generator.graph.edges.EdgeMethod;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.graph.resultfinding.ResultFindingImpl;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryObject;
import org.jacodb.api.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.codegenerator.Utils.loadOrCreateDataBase;
import static org.codegenerator.Utils.throwIf;

public class BuilderMethodSequenceFinder implements MethodSequenceFinder {
    private static final String BUILDER_NOT_FOUND = "Builder not found";

    private final String dbname = BuilderMethodSequenceFinder.class.getCanonicalName();
    private final Class<?>[] classes;
    private final LazyMethodFinder methodFinder;

    public BuilderMethodSequenceFinder(@NotNull Class<?> clazz, Class<?>... classes) {
        this.classes = classes;

        @NotNull List<BuilderInfo> builderInfoList = new JacoDBProxy(classes).findBuilderInfoList(clazz);

        checkInvariants(builderInfoList);

        methodFinder = new LazyMethodFinder(builderInfoList, new LazyGraph());
    }

    @Override
    public boolean canTry(TargetObject targetObject) {
        return true;
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull TargetObject targetObject, History<Executable> history) {
        Pair<BuilderInfo, Path> found = methodFinder.find(targetObject);
        BuilderInfo builderInfo = found.getFirst();
        Path path = found.getSecond();
        List<EdgeMethod> methods = path.getMethods();

        List<HistoryCall<Executable>> calls = new ArrayList<>();
        List<Object> suspect = new ArrayList<>();

        calls.add(new HistoryCall<>(history, builderInfo.constructor()));
        for (EdgeMethod method : methods) {
            calls.add(new HistoryCall<>(history, method.getMethod(), method.getArgs()));
            suspect.addAll(Arrays.asList(method.getArgs()));
        }
        calls.add(new HistoryCall<>(history, builderInfo.method()));

        Object object = targetObject.getObject();
        history.put(object, new HistoryObject<>(object, calls, BuilderMethodSequenceFinder.class));

        Object built = Utils.callSupplierWrapper(() -> builderInfo.method().invoke(path.getActualObject()));
        return new ResultFindingImpl(built, path.getDeviation(), suspect);
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull TargetObject targetObject, History<JcMethod> history) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            Pair<BuilderInfo, Path> found = methodFinder.find(targetObject);
            BuilderInfo builderInfo = found.getFirst();
            Path path = found.getSecond();
            List<EdgeMethod> methods = path.getMethods();

            Class<?> builderClazz = builderInfo.builder();
            JcClasspath classpath = Utils.toJcClasspath(db, ArrayUtils.add(classes, builderClazz));

            List<HistoryCall<JcMethod>> calls = new ArrayList<>();
            List<Object> suspect = new ArrayList<>();

            Executable constructor = builderInfo.constructor();
            Class<?> builder = constructor.getDeclaringClass();

            addMethod(Objects.requireNonNull(classpath.findClassOrNull(builder.getTypeName())), history, constructor, calls);
            addMethods(Objects.requireNonNull(classpath.findClassOrNull(builderClazz.getTypeName())), history, methods, calls, suspect);
            addMethod(Objects.requireNonNull(classpath.findClassOrNull(builder.getTypeName())), history, builderInfo.method(), calls);

            Object object = targetObject.getObject();
            history.put(object, new HistoryObject<>(object, calls, BuilderMethodSequenceFinder.class));

            Object built = Utils.callSupplierWrapper(() -> builderInfo.method().invoke(path.getActualObject()));
            return new ResultFindingImpl(built, path.getDeviation(), suspect);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (ExecutionException | IOException e) {
            throw new JacoDBException(e);
        }
    }

    private void addMethod(
            @NotNull JcClassOrInterface jcClassOrInterface,
            History<JcMethod> history,
            @NotNull Executable method,
            @NotNull List<HistoryCall<JcMethod>> calls
    ) {
        JcLookup<JcField, JcMethod> lookup = jcClassOrInterface.getLookup();

        JcMethod jcMethod = lookup.method(Utils.buildMethodName(method), Utils.buildDescriptor(method));
        calls.add(new HistoryCall<>(history, jcMethod));
    }

    private void addMethods(
            @NotNull JcClassOrInterface jcClassOrInterface,
            History<JcMethod> history,
            @NotNull List<EdgeMethod> methods,
            @NotNull List<HistoryCall<JcMethod>> calls,
            @NotNull List<Object> suspect
    ) {
        JcLookup<JcField, JcMethod> lookup = jcClassOrInterface.getLookup();

        for (EdgeMethod em : methods) {
            Object[] args = em.getArgs();
            calls.add(new HistoryCall<>(history, em.toJcMethod(lookup), args));
            suspect.addAll(Arrays.asList(args));
        }
    }

    private void checkInvariants(@NotNull List<BuilderInfo> builderInfoList) {
        throwIf(builderInfoList.isEmpty(), new InvariantCheckingException(BUILDER_NOT_FOUND));
    }

    private static class LazyMethodFinder {
        private Function<TargetObject, Pair<BuilderInfo, Path>> finder;

        private LazyMethodFinder(List<BuilderInfo> builderInfoList, LazyGraph lazyGraph) {
            initFinder(builderInfoList, lazyGraph);
        }

        private Pair<BuilderInfo, Path> find(TargetObject targetObject) {
            return finder.apply(targetObject);
        }

        private @NotNull Path find(
                @NotNull BuilderInfo builderInfo,
                @NotNull LazyGraph lazyGraph,
                TargetObject targetObject
        ) {
            Method builderBuildMethod = builderInfo.method();
            Executable builderConstructor = builderInfo.constructor();

            return lazyGraph.findPath(
                    targetObject,
                    createConstructorSupplier(builderConstructor),
                    createTerminationFunction(builderBuildMethod)
            );
        }

        private void initFinder(List<BuilderInfo> builderInfoList, LazyGraph lazyGraph) {
            finder = (TargetObject o) -> {
                for (BuilderInfo builderInfo : builderInfoList) {
                    try {
                        @NotNull Path path = find(builderInfo, lazyGraph, o);
                        finder = o1 -> new Pair<>(builderInfo, find(builderInfo, lazyGraph, o1));
                        return new Pair<>(builderInfo, path);
                    } catch (Exception e) {
                        // this block must be empty
                    }
                }
                throw new MethodSequenceNotFoundException();
            };
        }

        @Contract(pure = true)
        private static @NotNull Supplier<Object> createConstructorSupplier(Executable executable) {
            if (executable instanceof Method) {
                return () -> Utils.callSupplierWrapper(() -> ((Method) executable).invoke(null));
            }
            if (executable instanceof Constructor<?>) {
                return () -> Utils.callSupplierWrapper(((Constructor<?>) executable)::newInstance);
            }
            throw new IllegalArgumentException();
        }

        @Contract(pure = true)
        private static @NotNull UnaryOperator<Object> createTerminationFunction(Method method) {
            return o -> Utils.callSupplierWrapper(() -> method.invoke(o));
        }
    }
}
