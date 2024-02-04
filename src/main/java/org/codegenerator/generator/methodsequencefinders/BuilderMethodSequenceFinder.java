package org.codegenerator.generator.methodsequencefinders;

import kotlin.sequences.Sequence;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Call;
import org.codegenerator.Utils;
import org.codegenerator.exceptions.InvariantCheckingException;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.codegenerators.buildables.*;
import org.codegenerator.generator.graph.AssignableTypePropertyGrouper;
import org.codegenerator.generator.graph.EdgeMethod;
import org.codegenerator.generator.graph.StateGraph;
import org.codegenerator.history.History;
import org.jacodb.api.*;
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

public class BuilderMethodSequenceFinder implements MethodSequenceFinderInternal {
    private final String dbname = BuilderMethodSequenceFinder.class.getCanonicalName();
    private final Class<?> clazz;
    private final Class<?>[] classes;
    private final List<BuilderInfo> builderInfoList;

    public BuilderMethodSequenceFinder(@NotNull Class<?> clazz, Class<?>... classes) {
        this.clazz = clazz;
        this.classes = classes;

        builderInfoList = createBuilderInfoList();

        checkInvariants();
    }

    public List<Buildable> findBuildableList(@NotNull Object finalObject) {
        for (BuilderInfo builderInfo : builderInfoList) {
            try {
                List<EdgeMethod> edgeMethods = find(builderInfo, finalObject);
                return createBuildableList(edgeMethods, builderInfo);
            } catch (Exception e) {
                // this block must be empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }

    @Override
    public History<Executable> findReflectionCalls(@NotNull Object finalObject) {
        for (BuilderInfo builderInfo : builderInfoList) {
            try {
                List<EdgeMethod> edgeMethods = find(builderInfo, finalObject);
                List<Call<Executable>> calls = new ArrayList<>();
                calls.add(new Call<>(builderInfo.builderConstructor));
                edgeMethods.forEach(it -> calls.add(new Call<>(it.getMethod(), it.getArgs())));
//                return calls;
                return null;
            } catch (Exception e) {
                // this block must be empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }

    @Override
    public List<Call<JcMethod>> findJacoDBCalls(@NotNull Object finalObject) {
        for (BuilderInfo builderInfo : builderInfoList) {
            try {
                List<EdgeMethod> edgeMethods = find(builderInfo, finalObject);
                return createJacoDBCalls(builderInfo, edgeMethods);
            } catch (Exception e) {
                // this block must be empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }

    private @NotNull List<EdgeMethod> find(@NotNull BuilderInfo builderInfo, @NotNull Object finalObject) {
        Method builderBuildMethod = builderInfo.builderBuildMethod;
        StateGraph stateGraph = builderInfo.stateGraph;
        Executable builderConstructor = builderInfo.builderConstructor;

        UnaryOperator<Object> termination = createTerminationFunction(builderBuildMethod);
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        return stateGraph.findPath(assignableTypePropertyGrouper, createConstructorSupplier(builderConstructor), termination);
    }

    private @NotNull List<BuilderInfo> createBuilderInfoList() {
        List<BuilderInfo> builderInfoList1 = new ArrayList<>();
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            List<Class<?>> builderClasses = findBuilders(db);

            for (Class<?> builderClass : builderClasses) {
                Method buildMethod = findBuildMethod(builderClass);
                if (buildMethod == null) continue;

                Executable builderConstructor = findBuilderConstructor(db, builderClass);
                StateGraph stateGraph = new StateGraph();

                builderInfoList1.add(new BuilderInfo(builderClass, builderConstructor, buildMethod, stateGraph));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (IOException | ExecutionException e) {
            throw new JacoDBException(e);
        }
        return builderInfoList1;
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

    private @NotNull List<Call<JcMethod>> createJacoDBCalls(@NotNull BuilderInfo builderInfo, @NotNull List<EdgeMethod> methodList) {
        Class<?> builderClazz = builderInfo.builderClazz;
        Executable builderConstructor = builderInfo.builderConstructor;
        Class<?> builderConstructorClazz = builderConstructor.getDeclaringClass();

        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            JcClasspath classpath = createJcClasspath(db, builderClazz);

            List<Call<JcMethod>> callList = new ArrayList<>();

            JcClassOrInterface jcClassOrInterface = Objects.requireNonNull(classpath.findClassOrNull(builderConstructorClazz.getTypeName()));

            JcLookup<JcField, JcMethod> lookup = jcClassOrInterface.getLookup();

            JcMethod jcMethod = lookup.method(createMethodName(builderConstructor), Utils.buildDescriptor(builderConstructor));
            callList.add(new Call<>(jcMethod));

            if (builderClazz != builderConstructorClazz) {
                jcClassOrInterface = Objects.requireNonNull(classpath.findClassOrNull(builderClazz.getTypeName()));
            }
            lookup = jcClassOrInterface.getLookup();

            for (EdgeMethod edgeMethod : methodList) {
                Method method = edgeMethod.getMethod();
                Object[] args = edgeMethod.getArgs();
                jcMethod = lookup.method(method.getName(), Utils.buildDescriptor(method));
                callList.add(new Call<>(jcMethod, args));
            }
            return callList;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (ExecutionException | IOException e) {
            throw new JacoDBException(e);
        }
    }

    private Method findBuildMethod(@NotNull Class<?> cls) {
        return Arrays.stream(cls.getMethods())
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> ClassUtils.isAssignable(clazz, m.getReturnType()))
                .findFirst().orElse(null);
    }

    private @Nullable Executable findBuilderConstructor(JcDatabase db, @NotNull Class<?> builderClazz) throws ExecutionException, InterruptedException {
        for (Constructor<?> constructor : builderClazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) return constructor;
        }
        try {
            JcClasspath classpath = createJcClasspath(db, builderClazz);
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
        } catch (
                ClassNotFoundException |
                NoSuchMethodException e
        ) {
            throw new JacoDBException(e);
        }
    }

    private JcClasspath createJcClasspath(@NotNull JcDatabase db, Class<?> clazz) throws ExecutionException, InterruptedException {
        Class<?>[] localClasses = ArrayUtils.add(classes, clazz);
        localClasses = ArrayUtils.add(localClasses, clazz);
        List<File> fileList = Arrays.stream(localClasses).map(it ->
                Utils.callSupplierWrapper(() -> new File(it.getProtectionDomain().getCodeSource().getLocation().toURI()))
        ).collect(Collectors.toList());
        return db.asyncClasspath(fileList).get();
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

    @Contract(pure = true)
    private @NotNull String createMethodName(Executable executable) {
        if (executable instanceof Method) {
            return executable.getName();
        }
        if (executable instanceof Constructor<?>) {
            return "<init>";
        }
        throw new IllegalArgumentException();
    }

    private void checkInvariants() {
        throwIf(builderInfoList.isEmpty(), new InvariantCheckingException(BUILDER_NOT_FOUND));
    }

    public List<Class<?>> findBuilders(@NotNull JcDatabase db) throws ExecutionException, InterruptedException {
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
    }

    private JcDatabase loadOrCreateDataBase(String dbname) throws ExecutionException, InterruptedException {
        return Utils.loadOrCreateDataBase(dbname, Builders.INSTANCE, Usages.INSTANCE, InMemoryHierarchy.INSTANCE);
    }

    @Override
    public List<Object> findReflectionCallsInternal(@NotNull Object finalObject, History<Executable> history) {
        return null;
    }

    @Override
    public List<Object> findJacoDBCallsInternal(@NotNull Object finalObject, History<JcMethod> history) {
        return null;
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
