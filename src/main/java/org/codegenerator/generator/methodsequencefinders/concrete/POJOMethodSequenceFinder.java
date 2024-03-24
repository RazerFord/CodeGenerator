package org.codegenerator.generator.methodsequencefinders.concrete;

import com.rits.cloning.Cloner;
import org.codegenerator.ClonerUtilities;
import org.codegenerator.CommonUtils;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.generator.graph.LazyGraph;
import org.codegenerator.generator.graph.Path;
import org.codegenerator.generator.graph.edges.Edge;
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
import org.jacodb.impl.features.InMemoryHierarchy;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.codegenerator.CommonUtils.throwIf;

public class POJOMethodSequenceFinder implements MethodSequenceFinder {
    private final String dbname = POJOMethodSequenceFinder.class.getCanonicalName();
    private final LazyGraph lazyGraph = new LazyGraph();

    @Override
    public HistoryNode<Executable> createNode(
            @NotNull TargetObject targetObject,
            List<HistoryCall<Executable>> calls,
            HistoryNode<Executable> next
    ) {
        return new HistoryObject<>(
                targetObject.getObject(),
                calls,
                POJOMethodSequenceFinder.class,
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
    public RangeResultFinding findRanges(@NotNull Range rangeObject) {
        @NotNull Path path = lazyGraph.findPath(rangeObject);
        List<Object> suspects = new ArrayList<>();
        List<Edge<? extends Executable>> methods = path.getMethods();

        for (Edge<? extends Executable> edgeMethod : methods) {
            suspects.addAll(Arrays.asList(edgeMethod.getArgs()));
        }

        List<RangeResult> ranges = new ArrayList<>();

        Cloner cloner = ClonerUtilities.standard();
        Object from = cloner.deepClone(rangeObject.getFrom().getObject());
        Object to = from;
        for (int i = 0; i < methods.size(); i++) {
            to = cloner.deepClone(to);
            methods.get(i).invoke(to);
            RangeObject range = new RangeObject(new TargetObject(from), new TargetObject(to));
            List<Edge<? extends Executable>> allMethods = new ArrayList<>(methods.subList(0, i + 1));
            ranges.add(new RangeResult(range, allMethods));
        }

        return new RangeResultFindingImpl(rangeObject.getTo(), path.getDeviation(), POJOMethodSequenceFinder.class, suspects, ranges);
    }

    @Override
    public RangeResultFinding findRanges(TargetObject targetObject) {
        @NotNull Path path = lazyGraph.findPath(targetObject);

        List<Object> suspects = new ArrayList<>();
        List<Edge<? extends Executable>> methods = path.getMethods();

        for (Edge<? extends Executable> edgeMethod : methods) {
            suspects.addAll(Arrays.asList(edgeMethod.getArgs()));
        }

        List<RangeResult> ranges = new ArrayList<>();
        Cloner cloner = ClonerUtilities.standard();

        Edge<? extends Executable> constructor = path.getMethods().get(0);
        throwIf(!(constructor.getMethod() instanceof Constructor<?>), (Supplier<IllegalStateException>) IllegalStateException::new);

        Object from = constructor.invoke();
        Object to = from;
        ranges.add(new RangeResult(new RangeObject(new TargetObject(from), new TargetObject(to)), Collections.singletonList(constructor)));
        for (int i = 0; i < methods.size(); i++) {
            to = cloner.deepClone(to);
            methods.get(i).invoke(to);
            RangeObject range = new RangeObject(new TargetObject(from), new TargetObject(to));
            List<Edge<? extends Executable>> allMethods = new ArrayList<>(Collections.singletonList(constructor));
            allMethods.addAll(methods.subList(0, i + 1));
            ranges.add(new RangeResult(range, allMethods));
        }

        return new RangeResultFindingImpl(targetObject, path.getDeviation(), POJOMethodSequenceFinder.class, suspects, ranges);
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull TargetObject targetObject, History<Executable> history) {
        return findCallsInternal(targetObject, history, Edge::getMethod);
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull TargetObject targetObject, History<JcMethod> history) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            Class<?> clazz = targetObject.getClazz();
            JcClassOrInterface jcClassOrInterface = CommonUtils.toJcClassOrInterface(clazz, db);
            JcLookup<JcField, JcMethod> lookup = jcClassOrInterface.getLookup();

            return findCallsInternal(targetObject, history, o -> o.toJcMethod(lookup));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (ExecutionException | IOException e) {
            throw new JacoDBException(e);
        }
    }

    private JcDatabase loadOrCreateDataBase(String dbname) throws ExecutionException, InterruptedException {
        return CommonUtils.loadOrCreateDataBase(dbname, InMemoryHierarchy.INSTANCE);
    }

    private <T> @NotNull ResultFinding findCallsInternal(
            @NotNull TargetObject targetObject,
            History<T> history,
            @NotNull Function<Edge<? extends Executable>, T> toMethod
    ) {
        Path path = lazyGraph.findPath(targetObject);
        List<Edge<? extends Executable>> methods = path.getMethods();

        List<HistoryCall<T>> calls = new ArrayList<>();
        List<Object> suspect = new ArrayList<>();

        for (Edge<? extends Executable> em : methods) {
            Object[] args = em.getArgs();
            calls.add(new HistoryCall<>(history, toMethod.apply(em), args));
            suspect.addAll(Arrays.asList(args));
        }

        Object object = targetObject.getObject();
        history.put(object, new HistoryObject<>(object, calls, POJOMethodSequenceFinder.class));

        return new ResultFindingImpl(path.getActualObject(), path.getDeviation(), suspect);
    }
}
