package org.codegenerator.generator.methodsequencefinders;

import kotlin.sequences.Sequence;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Utils;
import org.codegenerator.generator.codegenerators.buildables.*;
import org.codegenerator.generator.graph.AssignableTypePropertyGrouper;
import org.codegenerator.generator.graph.EdgeMethod;
import org.codegenerator.generator.graph.StateGraph;
import org.jacodb.api.JcClassOrInterface;
import org.jacodb.api.JcClasspath;
import org.jacodb.api.JcDatabase;
import org.jacodb.api.JcMethod;
import org.jacodb.impl.JacoDB;
import org.jacodb.impl.JcSettings;
import org.jacodb.impl.features.Builders;
import org.jacodb.impl.features.BuildersExtension;
import org.jacodb.impl.features.InMemoryHierarchy;
import org.jacodb.impl.features.JcHierarchies;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.codegenerator.Utils.throwIf;

public class BuilderMethodSequenceFinder {
    private final String dbname = BuilderMethodSequenceFinder.class.getCanonicalName();
    private final Class<?> clazz;
    private final Class<?> builderClazz;
    private final Supplier<Object> constructorBuilder;
    private final Executable constructorExecutableBuilder;
    private final Method builderMethodBuild;
    private final StateGraph stateGraph;

    public BuilderMethodSequenceFinder(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        extractClassOrInterface();
        builderClazz = findBuilder();
        builderMethodBuild = findBuildMethod(builderClazz);
        constructorExecutableBuilder = findBuilderConstructor();
        constructorBuilder = createConstructorSupplier(constructorExecutableBuilder);
        stateGraph = new StateGraph(builderClazz);
        checkInvariants();
    }

    public List<Buildable> find(@NotNull Object finalObject) {
        Function<Object, Object> termination = createTerminationFunction(builderMethodBuild);
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        List<EdgeMethod> edgeMethods = stateGraph.findPath(assignableTypePropertyGrouper, constructorBuilder, termination);


        List<Buildable> buildableList = new ArrayList<>();

        if (edgeMethods.isEmpty()) {
            buildableList.add(new ReturnCreatingChainingMethod(builderClazz, constructorExecutableBuilder));
            buildableList.add(new FinalChainingMethod(builderMethodBuild));
            return buildableList;
        }

        buildableList.add(new BuilderCreationMethod(builderClazz, VARIABLE_NAME, constructorExecutableBuilder));

        boolean beginChain = false;
        int lastIndex = 0;
        for (int i = 0; i < edgeMethods.size(); i++) {
            EdgeMethod edgeMethod = edgeMethods.get(i);
            if (!beginChain) {
                if (edgeMethod.getMethod().getReturnType() == builderClazz) {
                    buildableList.add(new InitialChainingMethod(edgeMethod.getMethod(), VARIABLE_NAME, edgeMethod.getArgs()));
                    beginChain = true;
                    lastIndex = i + 1;
                } else {
                    buildableList.add(new MethodCall(edgeMethod.getMethod(), edgeMethod.getArgs()));
                }
            } else {
                if (edgeMethod.getMethod().getReturnType() == builderClazz) {
                    buildableList.add(new MiddleChainingMethod(edgeMethod.getMethod(), edgeMethod.getArgs()));
                } else {
                    buildableList.add(new FinalChainingMethod(edgeMethod.getMethod(), edgeMethod.getArgs()));
                    beginChain = false;
                    lastIndex = -1;
                }
            }
        }

        if (lastIndex != -1) {
            EdgeMethod edgeMethod = edgeMethods.get(lastIndex - 1);
            buildableList.set(lastIndex, new ReturnMiddleChainingMethod(edgeMethod.getMethod(), VARIABLE_NAME, edgeMethod.getArgs()));
            buildableList.add(new FinalChainingMethod(builderMethodBuild));
        } else {
            buildableList.add(new ReturnExpression(String.format("return %s.%s()", VARIABLE_NAME, builderMethodBuild.getName())));
        }

        return buildableList;
    }

    private Class<?> findBuilder() {
        return Arrays.stream(ArrayUtils.addAll(clazz.getClasses()))
                .filter(cls -> findBuildMethod(cls) != null)
                .findFirst().orElseThrow(() -> new RuntimeException(BUILDER_NOT_FOUND));
    }

    private Method findBuildMethod(@NotNull Class<?> cls) {
        return Arrays.stream(cls.getMethods())
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> ClassUtils.isAssignable(clazz, m.getReturnType()))
                .findFirst().orElse(null);
    }

    private Executable findBuilderConstructor() {
        for (Constructor<?> constructor : builderClazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) return constructor;
        }
        return Arrays.stream(ArrayUtils.addAll(clazz.getClasses(), clazz))
                .map(this::findBuilderConstructor)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(BUILDER_CONSTRUCTOR_FOUND));
    }

    private Method findBuilderConstructor(@NotNull Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> ClassUtils.isAssignable(builderClazz, method.getReturnType()))
                .findFirst().orElse(null);
    }

    @Contract(pure = true)
    private @NotNull Supplier<Object> createConstructorSupplier(Executable executable) {
        if (executable instanceof Method) {
            return () -> Utils.callSupplierWrapper(() -> ((Method) executable).invoke(null));
        }
        if (executable instanceof Constructor<?>) {
            return () -> Utils.callSupplierWrapper(((Constructor<?>) executable)::newInstance);
        }
        throw new IllegalArgumentException();
    }

    @Contract(pure = true)
    private @NotNull Function<Object, Object> createTerminationFunction(Method method) {
        return o -> Utils.callSupplierWrapper(() -> method.invoke(o));
    }

    private void checkInvariants() {
        throwIf(clazz.getConstructors().length > 0, new RuntimeException(CONSTRUCTOR_FOUND));
        throwIf(builderClazz == null, new RuntimeException(BUILDER_NOT_FOUND));
    }

    private void extractClassOrInterface() {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            List<File> fileList = Arrays.asList(new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()));
            JcClasspath classpath = db.asyncClasspath(fileList).get();
            JcClassOrInterface needle = Objects.requireNonNull(classpath.findClassOrNull(clazz.getTypeName()));
            BuildersExtension haystack = new BuildersExtension(classpath, JcHierarchies.asyncHierarchy(classpath).get());

            Sequence<JcMethod> jcMethodSequence = haystack.findBuildMethods(needle, true);
            Iterator<JcMethod> iterator = jcMethodSequence.iterator();
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JcDatabase loadOrCreateDataBase(String dbname) throws ExecutionException, InterruptedException {
        return JacoDB.async(new JcSettings()
                .useProcessJavaRuntime()
                .persistent(dbname)
                .installFeatures(Builders.INSTANCE, InMemoryHierarchy.INSTANCE)
        ).get();
    }

    private static final String VARIABLE_NAME = "object";
    private static final String CONSTRUCTOR_FOUND = "The constructor has been found. You can use a POJO generator";
    private static final String BUILDER_CONSTRUCTOR_FOUND = "Builder constructor not found";
    private static final String BUILDER_NOT_FOUND = "Builder not found";
}
