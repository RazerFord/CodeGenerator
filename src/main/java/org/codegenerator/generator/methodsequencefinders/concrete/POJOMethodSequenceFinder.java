package org.codegenerator.generator.methodsequencefinders.concrete;

import org.codegenerator.Utils;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.graph.ConstructorStateGraph;
import org.codegenerator.generator.graph.Path;
import org.codegenerator.generator.graph.LazyGraph;
import org.codegenerator.generator.graph.edges.Edge;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.graph.resultfinding.ResultFindingImpl;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryObject;
import org.jacodb.api.*;
import org.jacodb.impl.features.InMemoryHierarchy;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class POJOMethodSequenceFinder implements MethodSequenceFinder {
    private final String dbname = POJOMethodSequenceFinder.class.getCanonicalName();
    private final LazyGraph lazyGraph = new LazyGraph();
    private final ConstructorStateGraph constructorStateGraph = new ConstructorStateGraph();

    @Override
    public boolean canTry(TargetObject targetObject) {
        return true;
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull TargetObject targetObject, History<Executable> history) {
        return findCallsInternal(targetObject, history, Edge::getMethod);
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull TargetObject targetObject, History<JcMethod> history) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            Class<?> clazz = targetObject.getClazz();
            JcClassOrInterface jcClassOrInterface = Utils.toJcClassOrInterface(clazz, db);
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
        return Utils.loadOrCreateDataBase(dbname, InMemoryHierarchy.INSTANCE);
    }

    private <T> @NotNull ResultFinding findCallsInternal(
            @NotNull TargetObject targetObject,
            History<T> history,
            @NotNull Function<Edge<? extends Executable>, T> toMethod
    ) {
        Edge<? extends Executable> constructor = constructorStateGraph.findPath(targetObject);
        @NotNull Path path = lazyGraph.findPath(targetObject, constructor::invoke);
        List<? extends Edge<? extends Executable>> methods = path.getMethods();

        List<HistoryCall<T>> calls = new ArrayList<>();

        List<Object> suspect = new ArrayList<>(Arrays.asList(constructor.getArgs()));
        calls.add(new HistoryCall<>(history, toMethod.apply(constructor), constructor.getArgs()));

        for (Edge<? extends Executable> em : methods) {
            Object[] args = em.getArgs();
            calls.add(new HistoryCall<>(history, toMethod.apply(em), args));
            suspect.addAll(Arrays.asList(args));
        }

        Object object = targetObject.getObject();
        history.put(object, new HistoryObject<>(object, calls, POJOMethodSequenceFinder.class));

        int deviation = path.getDeviation();

        return new ResultFindingImpl(path.getActualObject(), deviation, suspect);
    }
}
