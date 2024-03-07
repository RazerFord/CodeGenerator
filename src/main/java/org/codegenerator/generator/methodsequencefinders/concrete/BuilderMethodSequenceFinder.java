package org.codegenerator.generator.methodsequencefinders.concrete;

import com.rits.cloning.Cloner;
import kotlin.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.codegenerator.ClonerUtilities;
import org.codegenerator.Utils;
import org.codegenerator.exceptions.InvariantCheckingException;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.graph.LazyMethodGraph;
import org.codegenerator.generator.graph.Path;
import org.codegenerator.generator.graph.edges.Edge;
import org.codegenerator.generator.graph.edges.EdgeExecutable;
import org.codegenerator.generator.graph.edges.EdgeMethod;
import org.codegenerator.generator.graph.resultfinding.RangeResultFinding;
import org.codegenerator.generator.graph.resultfinding.RangeResultFindingImpl;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.graph.resultfinding.ResultFindingImpl;
import org.codegenerator.generator.objectwrappers.*;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
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

        methodFinder = new LazyMethodFinder(builderInfoList, new LazyMethodGraph());
    }

    @Override
    public boolean canTry(TargetObject targetObject) {
        return true;
    }

    @Override
    public boolean canTry(Range range) {
        return true;
    }

    @Override
    public RangeResultFinding findRanges(@NotNull Range range) {
        Pair<BuilderInfo, Path> found = methodFinder.find(range.getFrom(), range.getTo());
        return toRangeResultFinding(found, range);
    }

    @Override
    public RangeResultFinding findRanges(TargetObject targetObject) {
        Pair<BuilderInfo, Path> found = methodFinder.find(targetObject);
        Object beginObject = methodFinder.newInstance();
        return toRangeResultFinding(found, new RangeObject(new TargetObject(beginObject), targetObject));
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

    private @NotNull RangeResultFinding toRangeResultFinding(
            @NotNull Pair<BuilderInfo, Path> found,
            @NotNull Range range
    ) {
        BuilderInfo builderInfo = found.getFirst();
        Path path = found.getSecond();
        List<EdgeMethod> methods = path.getMethods();

        List<Object> suspect = createSuspects(methods);
        List<RangeResult> ranges = createRanges(range.getFrom().getObject(), builderInfo, methods);

        return new RangeResultFindingImpl(range.getTo(), path.getDeviation(), BuilderMethodSequenceFinder.class, suspect, ranges);
    }

    private @NotNull List<Object> createSuspects(@NotNull List<EdgeMethod> methods) {
        List<Object> suspect = new ArrayList<>();
        for (EdgeMethod method : methods) {
            suspect.addAll(Arrays.asList(method.getArgs()));
        }
        return suspect;
    }

    private @NotNull List<RangeResult> createRanges(
            Object begin,
            @NotNull BuilderInfo info,
            @NotNull List<EdgeMethod> methods
    ) {
        List<RangeResult> ranges = new ArrayList<>();
        Cloner cloner = ClonerUtilities.standard();

        EdgeExecutable constructor = new EdgeExecutable(info.constructor());
        EdgeExecutable build = new EdgeExecutable(info.method());

        Object from = cloner.deepClone(begin);
        Object to = from;
        Object built = build.invoke(cloner.deepClone(from));
        ranges.add(new RangeResult(new RangeObject(new TargetObject(from), new TargetObject(built)), Arrays.asList(constructor, build)));
        for (int i = 0; i < methods.size(); i++) {
            to = cloner.deepClone(to);
            methods.get(i).invoke(to);
            built = build.invoke(cloner.deepClone(to));
            RangeObject range1 = new RangeObject(new TargetObject(from), new TargetObject(built));

            List<Edge<? extends Executable>> allMethods = new ArrayList<>();

            allMethods.add(constructor);
            allMethods.addAll(methods.subList(0, i + 1));
            allMethods.add(build);

            ranges.add(new RangeResult(range1, allMethods));
        }

        return ranges;
    }

    private static class LazyMethodFinder {
        private Consumer<Consumer<BuilderInfo>> finder;
        private final LazyMethodGraph lazyMethodGraph;

        private LazyMethodFinder(List<BuilderInfo> builderInfoList, LazyMethodGraph lazyMethodGraph) {
            initFinder(builderInfoList);
            this.lazyMethodGraph = lazyMethodGraph;
        }

        private Object newInstance() {
            Object[] beginObject = new Object[1];
            finder.accept(bi -> beginObject[0] = createConstructorSupplier(bi.constructor()).get());
            return beginObject[0];
        }

        private Pair<BuilderInfo, Path> find(TargetObject targetObject) {
            List<Pair<BuilderInfo, Path>> ref = new ArrayList<>(1);
            finder.accept(bi -> {
                Executable builderConstructor = bi.constructor();
                Object beginObject = createConstructorSupplier(builderConstructor).get();
                ref.add(new Pair<>(bi, find(bi, lazyMethodGraph, beginObject, targetObject)));
            });
            return ref.get(0);
        }

        private Pair<BuilderInfo, Path> find(TargetObject from, TargetObject to) {
            List<Pair<BuilderInfo, Path>> ref = new ArrayList<>(1);
            finder.accept(bi -> ref.add(new Pair<>(bi, find(bi, lazyMethodGraph, from.getObject(), to))));
            return ref.get(0);
        }

        private @NotNull Path find(
                @NotNull BuilderInfo builderInfo,
                @NotNull LazyMethodGraph lazyMethodGraph,
                @NotNull Object beginObject,
                TargetObject targetObject
        ) {
            Method builderBuildMethod = builderInfo.method();

            return lazyMethodGraph.findPath(
                    targetObject,
                    beginObject,
                    createTerminationFunction(builderBuildMethod)
            );
        }

        private void initFinder(List<BuilderInfo> builderInfoList) {
            finder = (Consumer<BuilderInfo> consumer) -> {
                for (BuilderInfo builderInfo : builderInfoList) {
                    try {
                        consumer.accept(builderInfo);
                        finder = (Consumer<BuilderInfo> consumer1) -> consumer1.accept(builderInfo);
                        return;
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
