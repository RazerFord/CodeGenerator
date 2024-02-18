package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.methodsequencefinders.internal.MethodSequenceFinderInternal;
import org.codegenerator.generator.methodsequencefinders.internal.NullMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.internal.ReflectionMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFinding;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PipelineMethodSequenceFinder implements MethodSequenceFinder {
    private final ReflectionMethodSequenceFinder reflectionMethodSequenceFinder = new ReflectionMethodSequenceFinder();
    private final NullMethodSequenceFinder nullMethodSequenceFinder = new NullMethodSequenceFinder();
    private final Map<Class<?>, MethodSequenceFinderInternal> cachedFinders = new IdentityHashMap<>();
    private final List<Function<Object, ? extends MethodSequenceFinderInternal>> methodSequenceFinderFunctions;

    public PipelineMethodSequenceFinder(List<Function<Object, ? extends MethodSequenceFinderInternal>> methodSequenceFinderFunctions) {
        this.methodSequenceFinderFunctions = methodSequenceFinderFunctions;
    }

    @Override
    public History<Executable> findReflectionCalls(@NotNull Object object) {
        History<Executable> history = new History<>();
        findReflectionCallsRecursive(object, history);
        return history;
    }

    @Override
    public History<JcMethod> findJacoDBCalls(@NotNull Object object) {
        History<JcMethod> history = new History<>();
        findJacoDBCallsRecursive(object, history);
        return history;
    }

    @Override
    public void registerFinder(Class<?> clazz, MethodSequenceFinderInternal finder) {
        cachedFinders.put(clazz, finder);
    }

    private void findReflectionCallsRecursive(@NotNull Object object, History<Executable> history) {
        findCallsRecursiveBase(object, (m, o) -> tryFindReflectionCalls(o, history, m));
    }

    private void findJacoDBCallsRecursive(@NotNull Object object, History<JcMethod> history) {
        findCallsRecursiveBase(object, (m, o) -> tryFindJacoDBCalls(o, history, m));
    }

    private void findCallsRecursiveBase(Object object, BiConsumer<MethodSequenceFinderInternal, Object> consumer) {
        if (object == null) {
            consumer.accept(nullMethodSequenceFinder, null);
            return;
        }
        Class<?> clazz = object.getClass();
        MethodSequenceFinderInternal methodSequenceFinder = cachedFinders.get(clazz);
        if (methodSequenceFinder != null) {
            consumer.accept(methodSequenceFinder, object);
            return;
        }
        for (Function<Object, ? extends MethodSequenceFinderInternal> function : methodSequenceFinderFunctions) {
            try {
                methodSequenceFinder = function.apply(object);
                if (methodSequenceFinder.canTry(object)) {
                    consumer.accept(methodSequenceFinder, object);
                    cachedFinders.put(clazz, methodSequenceFinder);
                    return;
                }
            } catch (Exception ignored) {
                // this code block is empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }


    private void tryFindReflectionCalls(
            Object object,
            History<Executable> history,
            @NotNull MethodSequenceFinderInternal methodSequenceFinder
    ) {
        ResultFinding resultFinding = methodSequenceFinder.findReflectionCallsInternal(object, history);
        List<Object> suspects = resultFinding.getSuspects();
        suspects.forEach(it -> findReflectionCallsRecursive(it, history));
        if (!resultFinding.isSuccess()) {
            useReflection(object, resultFinding.getTargetObject(), history)
                    .getSuspects().forEach(it -> findReflectionCallsRecursive(it, history));
        }
    }

    private void tryFindJacoDBCalls(
            Object object,
            History<JcMethod> history,
            @NotNull MethodSequenceFinderInternal methodSequenceFinder
    ) {
        ResultFinding resultFinding = methodSequenceFinder.findJacoDBCallsInternal(object, history);
        List<Object> suspects = resultFinding.getSuspects();
        suspects.forEach(it -> findJacoDBCallsRecursive(it, history));
        if (!resultFinding.isSuccess()) {
            useReflection(object, resultFinding.getTargetObject(), history)
                    .getSuspects().forEach(it -> findJacoDBCallsRecursive(it, history));
        }
    }

    @Contract(pure = true)
    private <T> @NotNull ResultFinding useReflection(
            Object expected,
            Object actual,
            @NotNull History<T> history
    ) {
        return reflectionMethodSequenceFinder.findSetter(expected, actual, history);
    }
}
