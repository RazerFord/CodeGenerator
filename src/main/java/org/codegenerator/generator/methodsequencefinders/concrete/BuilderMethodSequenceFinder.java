package org.codegenerator.generator.methodsequencefinders.concrete;

import com.rits.cloning.Cloner;
import org.apache.commons.lang3.ArrayUtils;
import org.codegenerator.ClonerUtilities;
import org.codegenerator.CommonUtils;
import org.codegenerator.exceptions.InvariantCheckingException;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.generator.BuilderInfo;
import org.codegenerator.generator.JacoDBProxy;
import org.codegenerator.generator.graph.LazyGraphCombinationBuilder;
import org.codegenerator.generator.graph.Path;
import org.codegenerator.generator.graph.edges.Edge;
import org.codegenerator.generator.graph.edges.EdgeExecutable;
import org.codegenerator.generator.graph.resultfinding.RangeResultFinding;
import org.codegenerator.generator.graph.resultfinding.RangeResultFindingImpl;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.graph.resultfinding.ResultFindingImpl;
import org.codegenerator.generator.objectwrappers.Range;
import org.codegenerator.generator.objectwrappers.RangeObject;
import org.codegenerator.generator.objectwrappers.RangeResult;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
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
import java.util.function.Supplier;

import static org.codegenerator.CommonUtils.loadOrCreateDataBase;
import static org.codegenerator.CommonUtils.throwIf;

public class BuilderMethodSequenceFinder implements MethodSequenceFinder {
    private static final String BUILDER_NOT_FOUND = "Builder not found";

    private final String dbname = BuilderMethodSequenceFinder.class.getCanonicalName();
    private final Class<?>[] classes;
    private final LazyGraphCombinationBuilder methodFinder;

    public BuilderMethodSequenceFinder(@NotNull Class<?> clazz, Class<?>... classes) {
        this.classes = classes;

        @NotNull List<BuilderInfo> builderInfoList = new JacoDBProxy(classes).findBuilderInfoList(clazz);

        checkInvariants(builderInfoList);

        methodFinder = new LazyGraphCombinationBuilder(builderInfoList);
    }

    @Override
    public HistoryNode<Executable> createNode(
            @NotNull TargetObject targetObject,
            List<HistoryCall<Executable>> calls,
            HistoryNode<Executable> next
    ) {
        return new HistoryObject<>(
                targetObject.getObject(),
                calls,
                BuilderMethodSequenceFinder.class,
                next
        );
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
        Path found = methodFinder.findPath(range);
        return toRangeResultFinding(found, range);
    }

    @Override
    public RangeResultFinding findRanges(TargetObject targetObject) {
        Path path = methodFinder.findPath(targetObject);
        Object beginObject = createConstructorSupplier(methodFinder.getBuilderInfo().constructor()).get();
        return toRangeResultFinding(path, new RangeObject(new TargetObject(beginObject), targetObject));
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull TargetObject targetObject, History<Executable> history) {
        Path path = methodFinder.findPath(targetObject);
        List<Edge<? extends Executable>> methods = path.getMethods();

        List<HistoryCall<Executable>> calls = new ArrayList<>();
        List<Object> suspect = new ArrayList<>();

        for (Edge<? extends Executable> method : methods) {
            calls.add(new HistoryCall<>(history, method.getMethod(), method.getArgs()));
            suspect.addAll(Arrays.asList(method.getArgs()));
        }

        Object object = targetObject.getObject();
        history.put(object, new HistoryObject<>(object, calls, BuilderMethodSequenceFinder.class));

        Object built = CommonUtils.callSupplierWrapper(() -> methodFinder.getBuilderInfo().method().invoke(path.getActualObject()));
        return new ResultFindingImpl(built, path.getDeviation(), suspect);
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull TargetObject targetObject, History<JcMethod> history) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            Path path = methodFinder.findPath(targetObject);
            BuilderInfo builderInfo = methodFinder.getBuilderInfo();
            List<Edge<? extends Executable>> methods = path.getMethods();

            Class<?> builderClazz = builderInfo.builder();
            JcClasspath classpath = CommonUtils.toJcClasspath(db, ArrayUtils.add(classes, builderClazz));

            List<HistoryCall<JcMethod>> calls = new ArrayList<>();
            List<Object> suspect = new ArrayList<>();

            addMethods(classpath, history, methods, calls, suspect);

            Object object = targetObject.getObject();
            history.put(object, new HistoryObject<>(object, calls, BuilderMethodSequenceFinder.class));

            Object built = CommonUtils.callSupplierWrapper(() -> builderInfo.method().invoke(path.getActualObject()));
            return new ResultFindingImpl(built, path.getDeviation(), suspect);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (ExecutionException | IOException e) {
            throw new JacoDBException(e);
        }
    }

    private void addMethods(
            @NotNull JcClasspath classpath,
            History<JcMethod> history,
            @NotNull List<Edge<? extends Executable>> methods,
            @NotNull List<HistoryCall<JcMethod>> calls,
            @NotNull List<Object> suspect
    ) {
        Map<Class<?>, JcLookup<JcField, JcMethod>> toJcLookup = new HashMap<>();
        for (Edge<? extends Executable> em : methods) {
            Object[] args = em.getArgs();
            Class<?> clazz = em.getMethod().getDeclaringClass();
            JcLookup<JcField, JcMethod> lookup = toJcLookup.computeIfAbsent(clazz, k -> Objects
                    .requireNonNull(classpath.findClassOrNull(clazz.getTypeName()))
                    .getLookup());
            calls.add(new HistoryCall<>(history, em.toJcMethod(lookup), args));
            suspect.addAll(Arrays.asList(args));
        }
    }

    private void checkInvariants(@NotNull List<BuilderInfo> builderInfoList) {
        throwIf(builderInfoList.isEmpty(), new InvariantCheckingException(BUILDER_NOT_FOUND));
    }

    private @NotNull RangeResultFinding toRangeResultFinding(
            @NotNull Path path,
            @NotNull Range range
    ) {
        List<Edge<? extends Executable>> methods = path.getMethods();

        List<Object> suspect = createSuspects(methods);
        List<RangeResult> ranges = buildRanges(range.getFrom().getObject(), methodFinder.getBuilderInfo(), methods);

        return new RangeResultFindingImpl(range.getTo(), path.getDeviation(), BuilderMethodSequenceFinder.class, suspect, ranges);
    }

    private @NotNull List<Object> createSuspects(@NotNull List<Edge<? extends Executable>> methods) {
        List<Object> suspect = new ArrayList<>();
        for (Edge<? extends Executable> method : methods) {
            suspect.addAll(Arrays.asList(method.getArgs()));
        }
        return suspect;
    }

    private @NotNull List<RangeResult> buildRanges(
            Object begin,
            @NotNull BuilderInfo info,
            @NotNull List<Edge<? extends Executable>> methods
    ) {
        List<RangeResult> ranges = new ArrayList<>();
        Cloner cloner = ClonerUtilities.standard();

        EdgeExecutable constructor = new EdgeExecutable(info.constructor());
        EdgeExecutable build = new EdgeExecutable(info.method());

        Object from = cloner.deepClone(begin);
        Object to = from;
        Object built = build.invoke(cloner.deepClone(from));
        ranges.add(new RangeResult(new RangeObject(new TargetObject(from), new TargetObject(built)), Arrays.asList(constructor, build)));
        for (int i = 0; i < methods.size()-1; i++) {
            to = cloner.deepClone(to);
            methods.get(i).invoke(to);
            built = build.invoke(cloner.deepClone(to));
            RangeObject range1 = new RangeObject(new TargetObject(from), new TargetObject(built));

            List<Edge<? extends Executable>> allMethods = new ArrayList<>();

            allMethods.add(constructor);
            allMethods.addAll(methods.subList(1, i + 1));
            allMethods.add(build);

            ranges.add(new RangeResult(range1, allMethods));
        }

        return ranges;
    }

    @Contract(pure = true)
    private static @NotNull Supplier<Object> createConstructorSupplier(Executable executable) {
        if (executable instanceof Method) {
            return () -> CommonUtils.callSupplierWrapper(() -> ((Method) executable).invoke(null));
        }
        if (executable instanceof Constructor<?>) {
            return () -> CommonUtils.callSupplierWrapper(((Constructor<?>) executable)::newInstance);
        }
        throw new IllegalArgumentException();
    }
}
