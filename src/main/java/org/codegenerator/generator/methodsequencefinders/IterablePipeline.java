package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.CustomLogger;
import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.methodsequencefinders.concrete.MethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.concrete.NullMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.concrete.ReflectionMethodSequenceFinder;
import org.codegenerator.history.History;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

public class IterablePipeline implements Iterable<History<Executable>> {
    private static final Logger LOGGER = CustomLogger.getLogger();

    private final ReflectionMethodSequenceFinder reflectionMethodSequenceFinder = new ReflectionMethodSequenceFinder();
    private final NullMethodSequenceFinder nullMethodSequenceFinder = new NullMethodSequenceFinder();
    private final Set<Class<?>> doNotCache = new HashSet<>();
    private final Map<Class<?>, MethodSequenceFinder> cachedFinders = new HashMap<>();
    private final List<Function<TargetObject, ? extends MethodSequenceFinder>> methodSequenceFinderFunctions;
    private final TargetObject targetObject;

    public IterablePipeline(
            List<Function<TargetObject, ? extends MethodSequenceFinder>> methodSequenceFinderFunctions,
            TargetObject targetObject
    ) {
        this.methodSequenceFinderFunctions = methodSequenceFinderFunctions;
        this.targetObject = targetObject;
    }

    private void findInternal(
            TargetObject target,
            History<Executable> history
    ) {
        if (nullMethodSequenceFinder.canTry(target)) {
            nullMethodSequenceFinder.findReflectionCallsInternal(target, history);
            return;
        }
        Class<?> clazz = target.getClazz();
        MethodSequenceFinder methodSequenceFinder = cachedFinders.get(clazz);
        if (methodSequenceFinder != null) {
            doFind(target, methodSequenceFinder, history);
            return;
        }
        for (Function<TargetObject, ? extends MethodSequenceFinder> function : methodSequenceFinderFunctions) {
            try {
                methodSequenceFinder = function.apply(target);
                if (methodSequenceFinder.canTry(target)) {
                    doFind(target, methodSequenceFinder, history);
                    cache(clazz, methodSequenceFinder);
                    return;
                }
            } catch (Exception ignored) {
                logging(methodSequenceFinder);
            }
        }
        throw new MethodSequenceNotFoundException();
    }

    private void doFind(TargetObject target, @NotNull MethodSequenceFinder finder, History<Executable> history) {
        ResultFinding result = finder
                .findReflectionCallsInternal(target, history);

        result.getSuspects()
                .forEach(it -> findInternal(new TargetObject(it), history));

        if (!result.isSuccess()) {
            useReflection(targetObject, new TargetObject(result.getActualObject()), history)
                    .getSuspects()
                    .forEach(it -> findInternal(new TargetObject(it), history));
        }
    }

    @Contract(pure = true)
    private <T> @NotNull ResultFinding useReflection(
            TargetObject expected,
            TargetObject actual,
            @NotNull History<T> history
    ) {
        return reflectionMethodSequenceFinder.findSetter(expected, actual, history);
    }

    @NotNull
    @Override
    public Iterator<History<Executable>> iterator() {
        return new CodeIterator(targetObject, this);
    }

    private void cache(Class<?> clazz, MethodSequenceFinder finder) {
        if (!doNotCache.contains(clazz)) {
            cachedFinders.put(clazz, finder);
        }
    }

    private static class CodeIterator implements Iterator<History<Executable>> {
        private final TargetObject targetObject;
        private final IterablePipeline iterablePipeline;

        private CodeIterator(@NotNull TargetObject targetObject, @NotNull IterablePipeline iterablePipeline) {
            this.targetObject = targetObject;
            this.iterablePipeline = iterablePipeline;

            iterablePipeline.doNotCache.add(targetObject.getClazz());
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Contract(pure = true)
        @Override
        public @Nullable History<Executable> next() {
            History<Executable> history = new History<>();
            iterablePipeline.findInternal(targetObject, history);
            return history;
        }
    }

    private static void logging(@Nullable MethodSequenceFinder finder) {
        if (finder != null) {
            String className = finder.getClass().getName();
            String msg = String.format("%s failed", className);
            LOGGER.warning(msg);
        }
    }
}
