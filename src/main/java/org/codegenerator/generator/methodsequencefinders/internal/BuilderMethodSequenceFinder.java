package org.codegenerator.generator.methodsequencefinders.internal;

import kotlin.Pair;
import kotlin.sequences.Sequence;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Utils;
import org.codegenerator.exceptions.InvariantCheckingException;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.codegenerators.buildables.*;
import org.codegenerator.generator.graph.AssignableTypePropertyGrouper;
import org.codegenerator.generator.graph.Path;
import org.codegenerator.generator.graph.StateGraph;
import org.codegenerator.generator.graph.edges.EdgeMethod;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFinding;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFindingImpl;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryObject;
import org.jacodb.api.*;
import org.jacodb.impl.features.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.codegenerator.Utils.throwIf;

public class BuilderMethodSequenceFinder implements MethodSequenceFinderInternal {
    private final String dbname = BuilderMethodSequenceFinder.class.getCanonicalName();
    private final ReflectionMethodSequenceFinder reflectionMethodSequenceFinder = new ReflectionMethodSequenceFinder();
    private final Class<?> clazz;
    private final Class<?>[] classes;
    private final LazyMethodFinder methodFinder;

    public BuilderMethodSequenceFinder(@NotNull Class<?> clazz, Class<?>... classes) {
        this.clazz = clazz;
        this.classes = classes;

        @NotNull List<BuilderInfo> builderInfoList = createBuilderInfoList();

        checkInvariants(builderInfoList);

        methodFinder = new LazyMethodFinder(builderInfoList, new StateGraph());
    }

    @Override
    public boolean canTry(Object object) {
        return true;
    }

    public List<Buildable> findBuildableList(@NotNull Object object) {
        Pair<BuilderInfo, Path> found = methodFinder.find(object);
        return createBuildableList(object, found);
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull Object finalObject, History<Executable> history) {
        Pair<BuilderInfo, Path> found = methodFinder.find(finalObject);
        BuilderInfo builderInfo = found.getFirst();
        Path path = found.getSecond();
        List<EdgeMethod> methods = path.getMethods();

        List<HistoryCall<Executable>> calls = new ArrayList<>();
        List<Object> suspect = new ArrayList<>();

        calls.add(new HistoryCall<>(history, builderInfo.builderConstructor));
        calls.add(new HistoryCall<>(history, builderInfo.builderBuildMethod));
        for (EdgeMethod method : methods) {
            calls.add(new HistoryCall<>(history, method.getMethod(), method.getArgs()));
            suspect.addAll(Arrays.asList(method.getArgs()));
        }

        history.put(finalObject, new HistoryObject<>(finalObject, calls));

        return new ResultFindingImpl(path.getActualObject(), path.getDeviation(), suspect);
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull Object finalObject, History<JcMethod> history) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            Pair<BuilderInfo, Path> found = methodFinder.find(finalObject);
            BuilderInfo builderInfo = found.getFirst();
            Path path = found.getSecond();
            List<EdgeMethod> methods = path.getMethods();

            Class<?> builderClazz = builderInfo.builderClazz;
            JcClasspath classpath = Utils.toJcClasspath(db, ArrayUtils.add(classes, builderClazz));

            List<HistoryCall<JcMethod>> calls = new ArrayList<>();
            List<Object> suspect = new ArrayList<>();

            Executable constructor = builderInfo.builderConstructor;
            Class<?> builder = constructor.getDeclaringClass();

            addConstructor(Objects.requireNonNull(classpath.findClassOrNull(builder.getTypeName())), history, constructor, calls);
            addMethods(Objects.requireNonNull(classpath.findClassOrNull(builderClazz.getTypeName())), history, methods, calls, suspect);

            history.put(finalObject, new HistoryObject<>(finalObject, calls));

            return new ResultFindingImpl(path.getActualObject(), path.getDeviation(), suspect);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (ExecutionException | IOException e) {
            throw new JacoDBException(e);
        }
    }

    private void addConstructor(
            @NotNull JcClassOrInterface jcClassOrInterface,
            History<JcMethod> history,
            @NotNull Executable constructor,
            @NotNull List<HistoryCall<JcMethod>> calls
    ) {
        JcLookup<JcField, JcMethod> lookup = jcClassOrInterface.getLookup();

        JcMethod jcMethod = lookup.method(Utils.buildMethodName(constructor), Utils.buildDescriptor(constructor));
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

    private @NotNull List<BuilderInfo> createBuilderInfoList() {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            List<BuilderInfo> builderInfoList1 = new ArrayList<>();
            List<Class<?>> builderClasses = findBuilders(db);

            for (Class<?> builderClass : builderClasses) {
                Method buildMethod = findBuildMethod(builderClass);
                if (buildMethod == null) continue;

                Executable builderConstructor = findBuilderConstructor(db, builderClass);

                builderInfoList1.add(new BuilderInfo(builderClass, builderConstructor, buildMethod));
            }
            return builderInfoList1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (IOException | ExecutionException e) {
            throw new JacoDBException(e);
        }
    }

    private @NotNull List<Buildable> createBuildableList(@NotNull Object object, @NotNull Pair<BuilderInfo, Path> found) {
        BuilderInfo builderInfo = found.getFirst();
        Path path = found.getSecond();
        List<EdgeMethod> methods = path.getMethods();

        List<Buildable> buildableList = new ArrayList<>();
        Class<?> builderClazz = builderInfo.builderClazz;
        Method builderBuildMethod = builderInfo.builderBuildMethod;
        Executable builderConstructor = builderInfo.builderConstructor;

        if (methods.isEmpty() && path.getDeviation() == 0) {
            buildableList.add(new ReturnCreatingChainingMethod(builderClazz, builderConstructor));
            buildableList.add(new FinalChainingMethod(builderBuildMethod));
            return buildableList;
        }
        buildableList.add(new BuilderCreationMethod(builderClazz, VARIABLE_NAME, builderConstructor));

        boolean beginChain = false;
        int lastIndex = 0;
        for (int i = 0; i < methods.size(); i++) {
            EdgeMethod edgeMethod = methods.get(i);
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

        if (lastIndex != -1 && path.getDeviation() == 0) {
            EdgeMethod edgeMethod = methods.get(lastIndex - 1);
            buildableList.set(lastIndex, new ReturnMiddleChainingMethod(edgeMethod.getMethod(), VARIABLE_NAME, edgeMethod.getArgs()));
            buildableList.add(new FinalChainingMethod(builderBuildMethod));
        } else {
            if (path.getDeviation() == 0) {
                buildableList.add(new ReturnExpression(String.format("%s.%s()", VARIABLE_NAME, builderBuildMethod.getName())));
            } else {
                String newVariableName = VARIABLE_NAME + "Built";
                buildableList.add(CodeBlockBuildable.createVariableBuilt(builderBuildMethod.getReturnType(), newVariableName, VARIABLE_NAME, builderBuildMethod.getName()));

                Object actual = Utils.callSupplierWrapper(() -> builderInfo.builderBuildMethod.invoke(path.getActualObject()));
                reflectionMethodSequenceFinder.updateBuildableList(newVariableName, object, actual, buildableList);

                buildableList.add(new ReturnExpression(newVariableName));
            }
        }

        return buildableList;
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
            JcClasspath classpath = Utils.toJcClasspath(db, ArrayUtils.add(classes, builderClazz));
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

    private void checkInvariants(@NotNull List<BuilderInfo> builderInfoList) {
        throwIf(builderInfoList.isEmpty(), new InvariantCheckingException(BUILDER_NOT_FOUND));
    }

    private List<Class<?>> findBuilders(@NotNull JcDatabase db) throws ExecutionException, InterruptedException {
        JcClasspath classpath = Utils.toJcClasspath(db, ArrayUtils.add(classes, clazz));

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

    private static class BuilderInfo {
        private final Class<?> builderClazz;
        private final Executable builderConstructor;
        private final Method builderBuildMethod;

        private BuilderInfo(
                Class<?> builderClazz,
                Executable builderConstructor,
                Method builderBuildMethod
        ) {
            this.builderClazz = builderClazz;
            this.builderConstructor = builderConstructor;
            this.builderBuildMethod = builderBuildMethod;
        }
    }

    private static class LazyMethodFinder {
        private Function<Object, Pair<BuilderInfo, Path>> finder;

        private LazyMethodFinder(List<BuilderInfo> builderInfoList, StateGraph stateGraph) {
            initFinder(builderInfoList, stateGraph);
        }

        private Pair<BuilderInfo, Path> find(Object object) {
            return finder.apply(object);
        }

        private @NotNull Path find(
                @NotNull BuilderInfo builderInfo,
                @NotNull StateGraph stateGraph,
                Object object
        ) {
            Method builderBuildMethod = builderInfo.builderBuildMethod;
            Executable builderConstructor = builderInfo.builderConstructor;

            return stateGraph.findPath(
                    new AssignableTypePropertyGrouper(object),
                    createConstructorSupplier(builderConstructor),
                    createTerminationFunction(builderBuildMethod)
            );
        }

        private void initFinder(List<BuilderInfo> builderInfoList, StateGraph stateGraph) {
            finder = o -> {
                for (BuilderInfo builderInfo : builderInfoList) {
                    try {
                        @NotNull Path path = find(builderInfo, stateGraph, o);
                        finder = o1 -> new Pair<>(builderInfo, find(builderInfo, stateGraph, o1));
                        return new Pair<>(builderInfo, path);
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

    private static final String VARIABLE_NAME = "object";
    private static final String BUILDER_CONSTRUCTOR_FOUND = "Builder constructor not found";
    private static final String BUILDER_NOT_FOUND = "Builder not found";
}
