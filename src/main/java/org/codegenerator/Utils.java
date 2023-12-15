package org.codegenerator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.swap;

public class Utils {
    public static <T extends RuntimeException> void throwIf(boolean cond, T exception) {
        if (cond) {
            throw exception;
        }
    }

    public static <T extends RuntimeException> void throwUnless(boolean cond, T exception) {
        throwIf(!cond, exception);
    }

    public static <E> E callSupplierWrapper(SupplierWrapper<E> supplierWrapper) {
        try {
            return supplierWrapper.get();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @FunctionalInterface
    public interface SupplierWrapper<T> {
        T get() throws Throwable;
    }

    public static <E> void callRunnableWrapper(RunnableWrapper<E> runnableWrapper) {
        try {
            runnableWrapper.run();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @FunctionalInterface
    public interface RunnableWrapper<T> {
        void run() throws Throwable;
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
}
