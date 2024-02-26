package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.methodsequencefinders.internal.MethodSequenceFinderInternal;
import org.codegenerator.generator.methodsequencefinders.internal.NullMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.internal.ReflectionMethodSequenceFinder;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
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
    private final List<Function<TargetObject, ? extends MethodSequenceFinderInternal>> methodSequenceFinderFunctions;

    public PipelineMethodSequenceFinder(List<Function<TargetObject, ? extends MethodSequenceFinderInternal>> methodSequenceFinderFunctions) {
        this.methodSequenceFinderFunctions = methodSequenceFinderFunctions;
    }

    @Override
    public History<Executable> findReflectionCalls(@NotNull TargetObject targetObject) {
        History<Executable> history = new History<>();
        findReflectionCallsRecursive(targetObject, history);
        return history;
    }

    @Override
    public History<JcMethod> findJacoDBCalls(@NotNull TargetObject targetObject) {
        History<JcMethod> history = new History<>();
        findJacoDBCallsRecursive(targetObject, history);
        return history;
    }

    @Override
    public void registerFinder(Class<?> clazz, MethodSequenceFinderInternal finder) {
        cachedFinders.put(clazz, finder);
    }

    private void findReflectionCallsRecursive(@NotNull TargetObject targetObject, History<Executable> history) {
        findCallsRecursiveBase(targetObject, (m, o) -> tryFindReflectionCalls(o, history, m));
    }

    private void findJacoDBCallsRecursive(@NotNull TargetObject targetObject, History<JcMethod> history) {
        findCallsRecursiveBase(targetObject, (m, o) -> tryFindJacoDBCalls(o, history, m));
    }

    private void findCallsRecursiveBase(@NotNull TargetObject targetObject, BiConsumer<MethodSequenceFinderInternal, TargetObject> consumer) {
        if (targetObject.getObject() == null) {
            consumer.accept(nullMethodSequenceFinder, targetObject);
            return;
        }
        Class<?> clazz = targetObject.getClazz();
        MethodSequenceFinderInternal methodSequenceFinder = cachedFinders.get(clazz);
        if (methodSequenceFinder != null) {
            consumer.accept(methodSequenceFinder, targetObject);
            return;
        }
        for (Function<TargetObject, ? extends MethodSequenceFinderInternal> function : methodSequenceFinderFunctions) {
            try {
                methodSequenceFinder = function.apply(targetObject);
                if (methodSequenceFinder.canTry(targetObject)) {
                    consumer.accept(methodSequenceFinder, targetObject);
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
            TargetObject targetObject,
            History<Executable> history,
            @NotNull MethodSequenceFinderInternal methodSequenceFinder
    ) {
        ResultFinding resultFinding = methodSequenceFinder.findReflectionCallsInternal(targetObject, history);
        List<Object> suspects = resultFinding.getSuspects();
        suspects.forEach(it -> findReflectionCallsRecursive(new TargetObject(it), history));
        if (!resultFinding.isSuccess()) {
            useReflection(targetObject, new TargetObject(resultFinding.getTargetObject()), history)
                    .getSuspects().forEach(it -> findReflectionCallsRecursive(new TargetObject(it), history));
        }
    }

    private void tryFindJacoDBCalls(
            TargetObject targetObject,
            History<JcMethod> history,
            @NotNull MethodSequenceFinderInternal methodSequenceFinder
    ) {
        ResultFinding resultFinding = methodSequenceFinder.findJacoDBCallsInternal(targetObject, history);
        List<Object> suspects = resultFinding.getSuspects();
        suspects.forEach(it -> findJacoDBCallsRecursive(new TargetObject(it), history));
        if (!resultFinding.isSuccess()) {
            useReflection(targetObject, new TargetObject(resultFinding.getTargetObject()), history)
                    .getSuspects().forEach(it -> findJacoDBCallsRecursive(new TargetObject(it), history));
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
}
