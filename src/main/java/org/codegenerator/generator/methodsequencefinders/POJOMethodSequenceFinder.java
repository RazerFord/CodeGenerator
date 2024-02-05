package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.Utils;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.generator.codegenerators.buildables.*;
import org.codegenerator.generator.graph.*;
import org.codegenerator.generator.graph.edges.EdgeConstructor;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryObject;
import org.jacodb.api.*;
import org.jacodb.impl.features.InMemoryHierarchy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class POJOMethodSequenceFinder implements MethodSequenceFinderInternal {
    private final String dbname = POJOMethodSequenceFinder.class.getCanonicalName();
    private final StateGraph stateGraph;
    private final PojoConstructorStateGraph pojoConstructorStateGraph;

    public POJOMethodSequenceFinder() {
        stateGraph = new StateGraph();
        pojoConstructorStateGraph = new PojoConstructorStateGraph();
    }

    public List<Buildable> findBuildableList(@NotNull Object finalObject) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        EdgeConstructor edgeConstructor = pojoConstructorStateGraph.findPath(assignableTypePropertyGrouper);
        List<EdgeMethod> methodList = stateGraph.findPath(assignableTypePropertyGrouper, edgeConstructor::invoke);

        Class<?> clazz = finalObject.getClass();
        List<Buildable> buildableList = new ArrayList<>();
        if (methodList.isEmpty()) {
            buildableList.add(new ReturnConstructorCall(clazz, edgeConstructor.getArgs()));
        } else {
            buildableList.add(new ConstructorCall(clazz, VARIABLE_NAME, edgeConstructor.getArgs()));
            methodList.forEach(it -> buildableList.add(new MethodCall(it.getMethod(), it.getArgs())));
            buildableList.add(new ReturnExpression(VARIABLE_NAME));
        }
        return buildableList;
    }

    @Override
    public History<Executable> findReflectionCalls(@NotNull Object finalObject) {
        History<Executable> history = new History<>();

        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        EdgeConstructor constructor = pojoConstructorStateGraph.findPath(assignableTypePropertyGrouper);
        List<EdgeMethod> methods = stateGraph.findPath(assignableTypePropertyGrouper, constructor::invoke);

        List<HistoryCall<Executable>> calls = new ArrayList<>();

        calls.add(new HistoryCall<>(history, constructor.getMethod(), constructor.getArgs()));
        methods.forEach(it -> calls.add(new HistoryCall<>(history, it.getMethod(), it.getArgs())));

        history.put(finalObject, new HistoryObject<>(finalObject, calls));

        return history;
    }

    @Override
    public History<JcMethod> findJacoDBCalls(@NotNull Object finalObject) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        EdgeConstructor constructor = pojoConstructorStateGraph.findPath(assignableTypePropertyGrouper);
        List<EdgeMethod> methods = stateGraph.findPath(assignableTypePropertyGrouper, constructor::invoke);

        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            Class<?> clazz = finalObject.getClass();
            List<File> fileList = Collections.singletonList(new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()));
            JcClasspath classpath = db.asyncClasspath(fileList).get();

            JcClassOrInterface jcClassOrInterface = Objects.requireNonNull(classpath.findClassOrNull(clazz.getTypeName()));

            List<HistoryCall<JcMethod>> calls = new ArrayList<>();
            History<JcMethod> history = new History<>();

            JcLookup<JcField, JcMethod> lookup = jcClassOrInterface.getLookup();
            JcMethod jcMethod = lookup.method("<init>", Utils.buildDescriptor(constructor.getMethod()));
            calls.add(new HistoryCall<>(history, jcMethod, constructor.getArgs()));
            for (EdgeMethod edgeMethod : methods) {
                Method method = edgeMethod.getMethod();
                JcMethod jcMethod1 = lookup.method(method.getName(), Utils.buildDescriptor(method));
                calls.add(new HistoryCall<>(history, jcMethod1, edgeMethod.getArgs()));
            }
            history.put(finalObject, new HistoryObject<>(finalObject, calls));

            return history;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (ExecutionException | IOException | URISyntaxException e) {
            throw new JacoDBException(e);
        }
    }

    @Override
    public List<Object> findReflectionCallsInternal(@NotNull Object object, History<Executable> history) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(object);
        EdgeConstructor constructor = pojoConstructorStateGraph.findPath(assignableTypePropertyGrouper);
        List<EdgeMethod> methods = stateGraph.findPath(assignableTypePropertyGrouper, constructor::invoke);

        List<HistoryCall<Executable>> calls = new ArrayList<>();
        List<Object> suspect = new ArrayList<>(Arrays.asList(constructor.getArgs()));

        calls.add(new HistoryCall<>(history, constructor.getMethod(), constructor.getArgs()));
        for (EdgeMethod method : methods) {
            calls.add(new HistoryCall<>(history, method.getMethod(), method.getArgs()));
            suspect.addAll(Arrays.asList(method.getArgs()));
        }

        history.put(object, new HistoryObject<>(object, calls));

        return suspect;
    }

    @Override
    public List<Object> findJacoDBCallsInternal(@NotNull Object finalObject, History<JcMethod> history) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        EdgeConstructor constructor = pojoConstructorStateGraph.findPath(assignableTypePropertyGrouper);
        List<EdgeMethod> methods = stateGraph.findPath(assignableTypePropertyGrouper, constructor::invoke);

        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            Class<?> clazz = finalObject.getClass();
            List<File> fileList = Collections.singletonList(new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()));
            JcClasspath classpath = db.asyncClasspath(fileList).get();

            JcClassOrInterface jcClassOrInterface = Objects.requireNonNull(classpath.findClassOrNull(clazz.getTypeName()));

            List<HistoryCall<JcMethod>> calls = new ArrayList<>();

            JcLookup<JcField, JcMethod> lookup = jcClassOrInterface.getLookup();
            JcMethod jcMethod = lookup.method("<init>", Utils.buildDescriptor(constructor.getMethod()));
            calls.add(new HistoryCall<>(history, jcMethod, constructor.getArgs()));
            List<Object> suspect = new ArrayList<>(Arrays.asList(constructor.getArgs()));
            for (EdgeMethod edgeMethod : methods) {
                Method method = edgeMethod.getMethod();
                Object[] args = edgeMethod.getArgs();
                JcMethod jcMethod1 = lookup.method(method.getName(), Utils.buildDescriptor(method));
                calls.add(new HistoryCall<>(history, jcMethod1, args));
                suspect.addAll(Arrays.asList(args));
            }
            history.put(finalObject, new HistoryObject<>(finalObject, calls));

            return suspect;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (ExecutionException | IOException | URISyntaxException e) {
            throw new JacoDBException(e);
        }
    }

    private JcDatabase loadOrCreateDataBase(String dbname) throws ExecutionException, InterruptedException {
        return Utils.loadOrCreateDataBase(dbname, InMemoryHierarchy.INSTANCE);
    }

    private static final String VARIABLE_NAME = "object";
}
