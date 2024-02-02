package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.Call;
import org.codegenerator.Utils;
import org.codegenerator.exceptions.InvariantCheckingException;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.generator.codegenerators.buildables.*;
import org.codegenerator.generator.graph.*;
import org.jacodb.api.*;
import org.jacodb.impl.features.InMemoryHierarchy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.codegenerator.Utils.throwIf;

public class POJOMethodSequenceFinder implements MethodSequenceFinder {
    private final String dbname = POJOMethodSequenceFinder.class.getCanonicalName();
    private final Class<?> clazz;
    private final StateGraph stateGraph;
    private final PojoConstructorStateGraph pojoConstructorStateGraph;

    public POJOMethodSequenceFinder(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        stateGraph = new StateGraph(clazz);
        pojoConstructorStateGraph = new PojoConstructorStateGraph(clazz);
        checkInvariants();
    }

    public List<Buildable> findBuildableList(@NotNull Object finalObject) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        EdgeConstructor edgeConstructor = pojoConstructorStateGraph.findPath(assignableTypePropertyGrouper);
        List<EdgeMethod> methodList = stateGraph.findPath(assignableTypePropertyGrouper, edgeConstructor::invoke);

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
    public List<Call<Executable>> findReflectionCalls(@NotNull Object finalObject) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        EdgeConstructor edgeConstructor = pojoConstructorStateGraph.findPath(assignableTypePropertyGrouper);
        List<EdgeMethod> methodList = stateGraph.findPath(assignableTypePropertyGrouper, edgeConstructor::invoke);

        List<Call<Executable>> callList = new ArrayList<>();
        callList.add(new Call<>(edgeConstructor.getMethod(), edgeConstructor.getArgs()));
        methodList.forEach(it -> callList.add(new Call<>(it.getMethod(), it.getArgs())));

        return callList;
    }

    @Override
    public List<Call<JcMethod>> findJacoDBCalls(@NotNull Object finalObject) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
            EdgeConstructor edgeConstructor = pojoConstructorStateGraph.findPath(assignableTypePropertyGrouper);
            List<EdgeMethod> methodList = stateGraph.findPath(assignableTypePropertyGrouper, edgeConstructor::invoke);

            List<File> fileList = Collections.singletonList(new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()));
            JcClasspath classpath = db.asyncClasspath(fileList).get();

            JcClassOrInterface jcClassOrInterface = Objects.requireNonNull(classpath.findClassOrNull(clazz.getTypeName()));

            List<Call<JcMethod>> callList = new ArrayList<>();

            JcLookup<JcField, JcMethod> lookup = jcClassOrInterface.getLookup();
            callList.add(new Call<>(lookup.method("<init>", Utils.buildDescriptor(edgeConstructor.getMethod())), edgeConstructor.getArgs()));
            methodList.forEach(it -> {
                Method method = it.getMethod();
                JcMethod jcMethod = lookup.method(method.getName(), Utils.buildDescriptor(method));
                callList.add(new Call<>(jcMethod, it.getArgs()));
            });
            return callList;
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

    private void checkInvariants() {
        int maxArguments = Arrays.stream(clazz.getDeclaredMethods()).filter(it -> Modifier.isPublic(it.getModifiers())).map(Method::getParameterCount).max(Comparator.naturalOrder()).orElse(0);
        int numberFields = clazz.getDeclaredFields().length;

        throwIf(maxArguments > numberFields, new InvariantCheckingException(NUM_ARG_GREATER_THEN_NUM_FIELDS));
    }

    private static final String VARIABLE_NAME = "object";
    private static final String NUM_ARG_GREATER_THEN_NUM_FIELDS = "The number of arguments is greater than the number of fields";
}
