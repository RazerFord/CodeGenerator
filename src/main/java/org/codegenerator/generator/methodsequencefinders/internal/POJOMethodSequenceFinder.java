package org.codegenerator.generator.methodsequencefinders.internal;

import org.codegenerator.Utils;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.generator.codegenerators.buildables.*;
import org.codegenerator.generator.graph.AssignableTypePropertyGrouper;
import org.codegenerator.generator.graph.ConstructorStateGraph;
import org.codegenerator.generator.graph.Path;
import org.codegenerator.generator.graph.StateGraph;
import org.codegenerator.generator.graph.edges.Edge;
import org.codegenerator.generator.graph.edges.EdgeConstructor;
import org.codegenerator.generator.graph.edges.EdgeMethod;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFinding;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFindingImpl;
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

public class POJOMethodSequenceFinder implements MethodSequenceFinderInternal {
    private final String dbname = POJOMethodSequenceFinder.class.getCanonicalName();
    private final ReflectionMethodSequenceFinder reflectionMethodSequenceFinder = new ReflectionMethodSequenceFinder();
    private final StateGraph stateGraph = new StateGraph();
    private final ConstructorStateGraph constructorStateGraph = new ConstructorStateGraph();

    @Override
    public boolean canTry(Object object) {
        return true;
    }

    public List<Buildable> findBuildableList(@NotNull Object object) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(object);
        EdgeConstructor edgeConstructor = constructorStateGraph.findPath(assignableTypePropertyGrouper);
        Path path = stateGraph.findPath(assignableTypePropertyGrouper, edgeConstructor::invoke);
        List<EdgeMethod> methods = path.getMethods();

        Class<?> clazz = object.getClass();
        List<Buildable> buildableList = new ArrayList<>();
        if (methods.isEmpty() && path.getDeviation() == 0) {
            buildableList.add(new ReturnConstructorCall(clazz, edgeConstructor.getArgs()));
        } else {
            buildableList.add(new ConstructorCall(clazz, VARIABLE_NAME, edgeConstructor.getArgs()));
            methods.forEach(it -> buildableList.add(new MethodCall(it.getMethod(), it.getArgs())));
            if (path.getDeviation() != 0) {
                reflectionMethodSequenceFinder.updateBuildableList(VARIABLE_NAME, object, path.getActualObject(), buildableList);
            }
            buildableList.add(new ReturnExpression(VARIABLE_NAME));
        }
        return buildableList;
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull Object object, History<Executable> history) {
        return findCallsInternal(object, history, Edge::getMethod);
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull Object object, History<JcMethod> history) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            Class<?> clazz = object.getClass();
            JcClassOrInterface jcClassOrInterface = Utils.toJcClassOrInterface(clazz, db);
            JcLookup<JcField, JcMethod> lookup = jcClassOrInterface.getLookup();

            return findCallsInternal(object, history, o -> o.toJcMethod(lookup));
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
            @NotNull Object object,
            History<T> history,
            @NotNull Function<Edge<? extends Executable>, T> toMethod
    ) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(object);
        Edge<? extends Executable> constructor = constructorStateGraph.findPath(assignableTypePropertyGrouper);
        @NotNull Path path = stateGraph.findPath(assignableTypePropertyGrouper, constructor::invoke);
        List<? extends Edge<? extends Executable>> methods = path.getMethods();

        List<HistoryCall<T>> calls = new ArrayList<>();

        List<Object> suspect = new ArrayList<>(Arrays.asList(constructor.getArgs()));
        calls.add(new HistoryCall<>(history, toMethod.apply(constructor), constructor.getArgs()));

        for (Edge<? extends Executable> em : methods) {
            Object[] args = em.getArgs();
            calls.add(new HistoryCall<>(history, toMethod.apply(em), args));
            suspect.addAll(Arrays.asList(args));
        }

        history.put(object, new HistoryObject<>(object, calls, POJOMethodSequenceFinder.class));

        int deviation = path.getDeviation();

        return new ResultFindingImpl(path.getActualObject(), deviation, suspect);
    }

    private static final String VARIABLE_NAME = "object";
}
