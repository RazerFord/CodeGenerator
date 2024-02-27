package org.codegenerator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.codegenerator.exceptions.CallWrapperException;
import org.jacodb.api.JcClassOrInterface;
import org.jacodb.api.JcClasspath;
import org.jacodb.api.JcDatabase;
import org.jacodb.api.JcFeature;
import org.jacodb.impl.JacoDB;
import org.jacodb.impl.JcSettings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.swap;

public class Utils {
    public static <T extends RuntimeException> void throwIf(boolean cond, T exception) {
        if (cond) {
            throw exception;
        }
    }

    public static <T extends RuntimeException> void throwIf(boolean cond, Supplier<T> supplier) {
        if (cond) {
            throw supplier.get();
        }
    }

    public static <T extends RuntimeException> void throwUnless(boolean cond, T exception) {
        throwIf(!cond, exception);
    }

    public static JcDatabase loadOrCreateDataBase(String dbname, JcFeature<?, ?>... jcFeatures) throws ExecutionException, InterruptedException {
        return JacoDB.async(new JcSettings()
                .useProcessJavaRuntime()
                .persistent(dbname)
                .installFeatures(jcFeatures)
        ).get();
    }

    public static @NotNull String buildDescriptor(@NotNull Executable executable) {
        StringBuilder descriptor = new StringBuilder("(");

        for (Class<?> c : executable.getParameterTypes()) {
            descriptor.append(toDescriptor(c));
        }
        descriptor.append(")");

        if (executable instanceof Constructor<?>) {
            descriptor.append("V");
        } else if (executable instanceof Method) {
            descriptor.append(toDescriptor(((Method) executable).getReturnType()));
        } else {
            throw new IllegalArgumentException();
        }
        return descriptor.toString();
    }

    @Contract(pure = true)
    public static @NotNull String buildMethodName(Executable executable) {
        if (executable instanceof Method) {
            return executable.getName();
        }
        if (executable instanceof Constructor<?>) {
            return "<init>";
        }
        throw new IllegalArgumentException();
    }

    public static JcClassOrInterface toJcClassOrInterface(
            @NotNull Class<?> targetClazz,
            @NotNull JcDatabase db,
            @NotNull Class<?>... otherClasses
    ) throws ExecutionException, InterruptedException {
        Class<?>[] classes = ArrayUtils.add(otherClasses, targetClazz);
        JcClasspath classpath = toJcClasspath(db, classes);
        return Objects.requireNonNull(classpath.findClassOrNull(targetClazz.getTypeName()));
    }

    public static JcClasspath toJcClasspath(@NotNull JcDatabase db, Class<?>... classes) throws ExecutionException, InterruptedException {
        List<File> fileList = Arrays.stream(classes)
                .filter(it -> it.getProtectionDomain().getCodeSource() != null)
                .map(it -> it.getProtectionDomain().getCodeSource().getLocation())
                .map(it -> Utils.callSupplierWrapper(() -> new File(it.toURI()))
                ).collect(Collectors.toList());
        return db.asyncClasspath(fileList).get();
    }

    public static <T, E extends Exception> T callSupplierWrapper(SupplierWrapper<T, E> supplierWrapper) {
        try {
            return supplierWrapper.get();
        } catch (Exception e) {
            throw new CallWrapperException(e);
        }
    }

    @FunctionalInterface
    public interface SupplierWrapper<T, E extends Exception> {
        T get() throws E;
    }

    public static <E extends Exception> void callRunnableWrapper(RunnableWrapper<E> runnableWrapper) {
        try {
            runnableWrapper.run();
        } catch (Exception e) {
            throw new CallWrapperException(e);
        }
    }

    @FunctionalInterface
    public interface RunnableWrapper<E extends Exception> {
        void run() throws E;
    }

    public static @NotNull List<List<Integer>> combinations(List<Integer> inputSet, int k) {
        List<List<Integer>> results = new ArrayList<>();
        combinationsInternal(inputSet, k, results, new ArrayList<>(), 0);
        return results;
    }

    private static void combinationsInternal(
            @NotNull List<Integer> inputSet, int k, List<List<Integer>> results, @NotNull ArrayList<Integer> accumulator, int index) {
        int needToAccumulate = k - accumulator.size();
        int canAccumulate = inputSet.size() - index;

        if (accumulator.size() == k) {
            results.add(new ArrayList<>(accumulator));
        } else if (needToAccumulate <= canAccumulate) {
            combinationsInternal(inputSet, k, results, accumulator, index + 1);
            accumulator.add(inputSet.get(index));
            combinationsInternal(inputSet, k, results, accumulator, index + 1);
            accumulator.remove(accumulator.size() - 1);
        }
    }

    public static @NotNull List<List<Integer>> permutations(List<Integer> sequence) {
        List<List<Integer>> permutations = new ArrayList<>();
        permutationsInternal(sequence, permutations, 0);
        return permutations;
    }

    private static void permutationsInternal(@NotNull List<Integer> sequence, List<List<Integer>> results, int index) {
        if (index == sequence.size() - 1) {
            results.add(new ArrayList<>(sequence));
        }

        for (int i = index; i < sequence.size(); i++) {
            swap(sequence, i, index);
            permutationsInternal(sequence, results, index + 1);
            swap(sequence, i, index);
        }
    }

    public static @NotNull List<List<Integer>> combinationsWithPermutations(@NotNull List<Integer> sequence) {
        return combinationsWithPermutations(sequence, sequence.size());
    }

    public static @NotNull List<List<Integer>> combinationsWithPermutations(@NotNull List<Integer> sequence, int maxLength) {
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i <= maxLength; i++) {
            for (List<Integer> combination : combinations(sequence, i)) {
                result.addAll(permutations(combination));
            }
        }
        return result;
    }

    public static Class<?> findClosestCommonSuper(@NotNull Class<?> a, Class<?> b) {
        while (!a.isAssignableFrom(b)) {
            a = a.getSuperclass();
        }
        return a;
    }

    public static Class<?> findClosestCommonSuperOrInterface(@NotNull Class<?> a, Class<?> b) {
        while (!a.isAssignableFrom(b)) {
            for (Class<?> i : a.getInterfaces()) {
                if (i.isAssignableFrom(b)) {
                    return i;
                }
            }
            a = a.getSuperclass();
        }
        return a;
    }

    private static final @NotNull @UnmodifiableView Map<Class<?>, String> PRIMITIVES_TO_DESCRIPTOR = getPrimitiveToDescriptor();

    private static @NotNull @UnmodifiableView Map<Class<?>, String> getPrimitiveToDescriptor() {
        Map<Class<?>, String> map = new HashMap<>();

        map.put(byte.class, "B");
        map.put(char.class, "C");
        map.put(double.class, "D");
        map.put(float.class, "F");
        map.put(int.class, "I");
        map.put(long.class, "J");
        map.put(short.class, "S");
        map.put(void.class, "V");
        map.put(boolean.class, "Z");

        return Collections.unmodifiableMap(map);
    }

    private static @NotNull String toDescriptor(@NotNull Class<?> c) {
        String desc = PRIMITIVES_TO_DESCRIPTOR.get(c);
        if (desc != null) return desc;

        if (c.isArray()) {
            int deep = 0;
            for (; c.isArray(); c = c.getComponentType()) {
                deep++;
            }
            return StringUtils.repeat('[', deep) + "L" + c.getName().replace(".", "/") + ";";
        } else {
            return "L" + c.getName().replace(".", "/") + ";";
        }
    }
}
