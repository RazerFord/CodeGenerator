package org.codegenerator.generator.methodsequencefinders.concrete;

import kotlin.sequences.Sequence;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Utils;
import org.codegenerator.exceptions.JacoDBException;
import org.codegenerator.generator.graph.edges.EdgeExecutable;
import org.codegenerator.history.*;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.codegenerator.Utils.throwIf;

public class JacoDBProxy {
    private static final String BUILDER_CONSTRUCTOR_FOUND = "Builder constructor not found";

    private final String dbname = BuilderMethodSequenceFinder.class.getCanonicalName();
    private final Class<?>[] classes;

    public JacoDBProxy(@NotNull Class<?>... classes) {
        this.classes = classes;
    }

    public History<JcMethod> historyToJcHistory(@NotNull History<Executable> history) {
        History<JcMethod> jcMethodHistory = new History<>();
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            for (Map.Entry<Object, HistoryNode<Executable>> e : history.entrySet()) {
                HistoryNode<JcMethod> node = toJacoDBNode(e, e.getValue(), db, jcMethodHistory);
                jcMethodHistory.put(e.getKey(), node);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JacoDBException(e);
        } catch (IOException | ExecutionException e) {
            throw new JacoDBException(e);
        }
        return jcMethodHistory;
    }

    public List<BuilderInfo> findBuilderInfoList(Class<?> clazz) {
        return Collections.unmodifiableList(createBuilderInfoList(clazz));
    }

    private @NotNull List<BuilderInfo> createBuilderInfoList(Class<?> clazz) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            List<BuilderInfo> builderInfoList1 = new ArrayList<>();
            List<Class<?>> builderClasses = findBuilders(db, clazz);

            for (Class<?> builderClass : builderClasses) {
                Method buildMethod = findBuildMethod(builderClass, clazz);
                if (buildMethod == null) continue;

                Executable builderConstructor = findBuilderConstructor(db, builderClass, clazz);

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

    private Method findBuildMethod(@NotNull Class<?> cls, Class<?> clazz) {
        return Arrays.stream(cls.getMethods())
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> ClassUtils.isAssignable(clazz, m.getReturnType()))
                .findFirst()
                .orElse(null);
    }

    private @NotNull Executable findBuilderConstructor(JcDatabase db, @NotNull Class<?> builderClazz, Class<?> clazz) throws ExecutionException, InterruptedException {
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


            return loadedClass.getMethod(jcMethod.getName(), getParameterTypes(jcMethod, classLoader));
        } catch (
                ClassNotFoundException |
                NoSuchMethodException e
        ) {
            throw new JacoDBException(e);
        }
    }

    private Class<?> @NotNull [] getParameterTypes(@NotNull JcMethod jcMethod, ClassLoader classLoader) {
        int[] index = new int[]{0};
        Class<?>[] classes1 = new Class[jcMethod.getParameters().size()];

        jcMethod.getParameters()
                .forEach(it -> classes1[index[0]++] = loadClass(it, classLoader));

        return classes1;
    }

    private static Class<?> loadClass(JcParameter parameter, ClassLoader classLoader) {
        return Utils.callSupplierWrapper(() -> classLoader.loadClass(parameter.getType().getTypeName()));
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

    private List<Class<?>> findBuilders(@NotNull JcDatabase db, Class<?> clazz) throws ExecutionException, InterruptedException {
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

    @Nullable
    private HistoryNode<JcMethod> toJacoDBNode(
            Map.@NotNull Entry<Object, HistoryNode<Executable>> e,
            HistoryNode<Executable> old, JcDatabase db, History<JcMethod> jcMethodHistory
    ) throws ExecutionException, InterruptedException {
        HistoryNode<JcMethod> node = null;
        switch (e.getValue().getType()) {
            case ARRAY:
                node = new HistoryArray<>(old.getObject(), old.getCreatorType());
                break;
            case OBJECT:
                node = toJacoDBNode(db, jcMethodHistory, old);
                break;
            case PRIMITIVE:
                node = new HistoryPrimitive<>(old.getObject(), old.getCreatorType());
                break;
        }
        return node;
    }

    @Contract("_, _, _ -> new")
    private @NotNull HistoryNode<JcMethod> toJacoDBNode(
            JcDatabase db,
            History<JcMethod> history,
            @NotNull HistoryNode<Executable> node
    ) throws ExecutionException, InterruptedException {
        List<Class<?>> classes1 = extractClasses(node);

        JcClasspath cp = Utils.toJcClasspath(db, classes1.toArray(new Class[]{}));

        Map<String, JcLookup<JcField, JcMethod>> toLookup = extractLookup(classes1, cp);

        List<HistoryCall<JcMethod>> calls = toJacoDBHistoryCalls(history, node, toLookup);

        List<SetterUsingReflection<JcMethod>> setters = toJacoDBsettersUsingReflection(history, node);

        HistoryNode<JcMethod> next = getNext(db, history, node);

        return new HistoryObject<>(node.getObject(), calls, setters, node.getCreatorType(), next);
    }

    @Nullable
    private HistoryNode<JcMethod> getNext(JcDatabase db, History<JcMethod> history, @NotNull HistoryNode<Executable> node) throws ExecutionException, InterruptedException {
        HistoryNode<Executable> nxt = node.nextNode();
        if (nxt != null) {
            return toJacoDBNode(db, history, nxt);
        }
        return null;
    }

    @NotNull
    private static List<SetterUsingReflection<JcMethod>> toJacoDBsettersUsingReflection(History<JcMethod> history, @NotNull HistoryNode<Executable> node) {
        return node.getSetterUsingReflections()
                .stream()
                .map(s -> new SetterUsingReflection<>(history, s.getField(), s.getSetObject()))
                .collect(Collectors.toList());
    }

    @NotNull
    private static List<HistoryCall<JcMethod>> toJacoDBHistoryCalls(
            History<JcMethod> history,
            @NotNull HistoryNode<Executable> node,
            Map<String, JcLookup<JcField, JcMethod>> toLookup
    ) {
        return node.getHistoryCalls()
                .stream()
                .map(call -> new EdgeExecutable(call.getMethod(), call.getArgs()))
                .map(e -> {
                    JcMethod jc = e.toJcMethod(toLookup.get(e.getMethod().getDeclaringClass().getName()));
                    return new HistoryCall<>(history, jc, e.getArgs());
                })
                .collect(Collectors.toList());
    }

    @NotNull
    private static Map<String, JcLookup<JcField, JcMethod>> extractLookup(@NotNull List<Class<?>> classes1, JcClasspath cp) {
        return classes1.stream()
                .map(c -> cp.findClassOrNull(c.getName()))
                .filter(Objects::nonNull)
                .filter(new Predicate<JcClassOrInterface>() {
                    final Set<String> contains = new HashSet<>();

                    @Override
                    public boolean test(JcClassOrInterface jcClassOrInterface) {
                        return contains.add(jcClassOrInterface.getName());
                    }
                })
                .collect(Collectors.toMap(JcClassOrInterface::getName, JcClassOrInterface::getLookup));
    }

    @NotNull
    private static List<Class<?>> extractClasses(@NotNull HistoryNode<Executable> node) {
        return node.getHistoryCalls()
                .stream()
                .map(HistoryCall::getMethod)
                .map(Executable::getDeclaringClass)
                .collect(Collectors.toList());
    }
}
