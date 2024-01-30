package org.codegenerator.generator.methodsequencefinders;

import kotlin.sequences.Sequence;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Utils;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.exceptions.PathNotFindException;
import org.codegenerator.generator.codegenerators.buildables.*;
import org.codegenerator.generator.graph.AssignableTypePropertyGrouper;
import org.codegenerator.generator.graph.EdgeMethod;
import org.codegenerator.generator.graph.StateGraph;
import org.jacodb.api.*;
import org.jacodb.impl.JacoDB;
import org.jacodb.impl.JcSettings;
import org.jacodb.impl.features.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.codegenerator.Utils.throwIf;

public class BuilderMethodSequenceFinder {
    private final String dbname = BuilderMethodSequenceFinder.class.getCanonicalName();
    private final Class<?> clazz;
    private final Class<?>[] classes;
    private final List<BuilderInfo> builderInfoList = new ArrayList<>();

    public BuilderMethodSequenceFinder(@NotNull Class<?> clazz, Class<?>... classes) {
        this.clazz = clazz;
        this.classes = classes;
        List<Class<?>> builderClasses = findBuilders();
        for (Class<?> builderClass : builderClasses) {
            Method buildMethod = findBuildMethod(builderClass);
            if (buildMethod == null) continue;
            Executable builderConstructor = findBuilderConstructor(builderClass);
            StateGraph stateGraph = new StateGraph(builderClass);
            builderInfoList.add(new BuilderInfo(builderClass, builderConstructor, buildMethod, stateGraph));
        }
        checkInvariants();
    }

    public List<Buildable> find(@NotNull Object finalObject) {
        for (BuilderInfo builderInfo : builderInfoList) {
            try {
                Method builderBuildMethod = builderInfo.builderBuildMethod;
                StateGraph stateGraph = builderInfo.stateGraph;
                Executable builderConstructor = builderInfo.builderConstructor;

                UnaryOperator<Object> termination = createTerminationFunction(builderBuildMethod);
                AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
                List<EdgeMethod> edgeMethods = stateGraph.findPath(assignableTypePropertyGrouper, createConstructorSupplier(builderConstructor), termination);

                return createBuildableList(edgeMethods, builderInfo);
            } catch (Exception e) {
                // this block must be empty
            }
        }
        throw new PathNotFindException();
    }

    private @NotNull List<Buildable> createBuildableList(@NotNull List<EdgeMethod> edgeMethods, @NotNull BuilderInfo builderInfo) {
        List<Buildable> buildableList = new ArrayList<>();
        Class<?> builderClazz = builderInfo.builderClazz;
        Method builderBuildMethod = builderInfo.builderBuildMethod;
        Executable builderConstructor = builderInfo.builderConstructor;

        if (edgeMethods.isEmpty()) {
            buildableList.add(new ReturnCreatingChainingMethod(builderClazz, builderConstructor));
            buildableList.add(new FinalChainingMethod(builderBuildMethod));
            return buildableList;
        }

        buildableList.add(new BuilderCreationMethod(builderClazz, VARIABLE_NAME, builderConstructor));

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
            buildableList.add(new FinalChainingMethod(builderBuildMethod));
        } else {
            buildableList.add(new ReturnExpression(String.format("%s.%s()", VARIABLE_NAME, builderBuildMethod.getName())));
        }

        return buildableList;
    }

    private Method findBuildMethod(@NotNull Class<?> cls) {
        return Arrays.stream(cls.getMethods())
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> ClassUtils.isAssignable(clazz, m.getReturnType()))
                .findFirst().orElse(null);
    }

    private @Nullable Executable findBuilderConstructor(@NotNull Class<?> builderClazz) {
        for (Constructor<?> constructor : builderClazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) return constructor;
        }
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            Class<?>[] localClasses = ArrayUtils.add(classes, builderClazz);
            localClasses = ArrayUtils.add(localClasses, clazz);
            List<File> fileList = Arrays.stream(localClasses).map(it ->
                    Utils.callSupplierWrapper(() -> new File(it.getProtectionDomain().getCodeSource().getLocation().toURI()))
            ).collect(Collectors.toList());
            JcClasspath classpath = db.asyncClasspath(fileList).get();
            JcClassOrInterface jcClassOrInterface = Objects.requireNonNull(classpath.findClassOrNull(builderClazz.getTypeName()));

            SyncUsagesExtension haystack = new SyncUsagesExtension(JcHierarchies.asyncHierarchy(classpath).get(), classpath);
            List<JcMethod> needles = jcClassOrInterface.getDeclaredMethods().stream()
                    .filter(it -> it.isConstructor() && !it.isSynthetic()).collect(Collectors.toList());

            JcMethod jcMethod = findMethodCreatingBuilder(haystack, needles);
            ClassLoader classLoader = clazz.getClassLoader();
            Class<?> loadedClass = classLoader.loadClass(jcMethod.getEnclosingClass().getName());

            int[] index = new int[]{0};
            Class<?>[] classes1 = new Class[jcMethod.getParameters().size()];
            jcMethod.getParameters()
                    .forEach(it -> classes1[index[0]++] = Utils.callSupplierWrapper(() -> classLoader.loadClass(it.getName())));

            return loadedClass.getMethod(jcMethod.getName(), classes1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (
                IOException |
                ExecutionException |
                ClassNotFoundException |
                NoSuchMethodException e
        ) {
            throw new JacoDBException(e);
        }
    }

    private JcMethod findMethodCreatingBuilder(SyncUsagesExtension haystack, @NotNull List<JcMethod> needles) {
        List<JcMethod> usages = new ArrayList<>();
        for (JcMethod needle : needles) {
            Sequence<JcMethod> jcMethodSequence = haystack.findUsages(needle);
            Iterator<JcMethod> it = jcMethodSequence.iterator();

            while (it.hasNext()) {
                usages.add(it.next());
            }
        }
        throwIf(usages.isEmpty(), new IllegalStateException(BUILDER_CONSTRUCTOR_FOUND));
        @NotNull List<JcMethod> finalNeedles = usages.stream().filter(JcMethod::isConstructor).collect(Collectors.toList());
        return usages.stream().filter(JcAccessible::isPublic).findFirst().orElseGet(() -> findMethodCreatingBuilder(haystack, finalNeedles));
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
    private @NotNull UnaryOperator<Object> createTerminationFunction(Method method) {
        return o -> Utils.callSupplierWrapper(() -> method.invoke(o));
    }

    private void checkInvariants() {
        throwIf(builderInfoList.isEmpty(), new RuntimeException(BUILDER_NOT_FOUND));
    }

    public List<Class<?>> findBuilders() {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            List<File> fileList = Arrays.stream(ArrayUtils.addAll(classes, clazz)).map(it ->
                    Utils.callSupplierWrapper(() -> new File(it.getProtectionDomain().getCodeSource().getLocation().toURI()))
            ).collect(Collectors.toList());
            JcClasspath classpath = db.asyncClasspath(fileList).get();
            JcClassOrInterface needle = Objects.requireNonNull(classpath.findClassOrNull(clazz.getTypeName()));
            BuildersExtension haystack = new BuildersExtension(classpath, JcHierarchies.asyncHierarchy(classpath).get());

            Sequence<JcMethod> jcMethodSequence = haystack.findBuildMethods(needle, true);
            Iterator<JcMethod> iterator = jcMethodSequence.iterator();
            List<JcMethod> methods = new ArrayList<>();
            iterator.forEachRemaining(methods::add);
            ClassLoader classLoader = clazz.getClassLoader();
            return methods.stream()
                    .map(it -> Utils.callSupplierWrapper(() -> classLoader.loadClass(it.getEnclosingClass().getName())))
                    .collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (IOException | ExecutionException e) {
            throw new JacoDBException(e);
        }
    }

    private JcDatabase loadOrCreateDataBase(String dbname) throws ExecutionException, InterruptedException {
        return JacoDB.async(new JcSettings()
                .useProcessJavaRuntime()
                .persistent(dbname)
                .installFeatures(Builders.INSTANCE, Usages.INSTANCE, InMemoryHierarchy.INSTANCE)
        ).get();
    }

    private static class BuilderInfo {
        private final Class<?> builderClazz;
        private final Executable builderConstructor;
        private final Method builderBuildMethod;
        private final StateGraph stateGraph;

        private BuilderInfo(
                Class<?> builderClazz,
                Executable builderConstructor,
                Method builderBuildMethod,
                StateGraph stateGraph
        ) {
            this.builderClazz = builderClazz;
            this.builderConstructor = builderConstructor;
            this.builderBuildMethod = builderBuildMethod;
            this.stateGraph = stateGraph;
        }
    }

    private static final String VARIABLE_NAME = "object";
    private static final String BUILDER_CONSTRUCTOR_FOUND = "Builder constructor not found";
    private static final String BUILDER_NOT_FOUND = "Builder not found";
}
