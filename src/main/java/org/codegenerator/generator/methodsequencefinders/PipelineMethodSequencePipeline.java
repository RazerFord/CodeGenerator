package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.CustomLogger;
import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.methodsequencefinders.concrete.MethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.concrete.NullMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.concrete.ReflectionMethodSequenceFinder;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class PipelineMethodSequencePipeline implements MethodSequencePipeline {
    private static final Logger LOGGER = CustomLogger.getLogger();

    private final ReflectionMethodSequenceFinder reflectionMethodSequenceFinder = new ReflectionMethodSequenceFinder();
    private final NullMethodSequenceFinder nullMethodSequenceFinder = new NullMethodSequenceFinder();
    private final Map<Class<?>, MethodSequenceFinder> cachedFinders = new IdentityHashMap<>();
    private final List<Function<TargetObject, ? extends MethodSequenceFinder>> methodSequenceFinderFunctions;

    public PipelineMethodSequencePipeline(List<Function<TargetObject, ? extends MethodSequenceFinder>> methodSequenceFinderFunctions) {
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
    public void registerFinderForClass(Class<?> clazz, MethodSequenceFinder finder) {
        cachedFinders.put(clazz, finder);
    }

    @Override
    public void resetFindersForClasses() {
        cachedFinders.clear();
    }

    @Override
    public List<Function<TargetObject, ? extends MethodSequenceFinder>> finders() {
        return Collections.unmodifiableList(methodSequenceFinderFunctions);
    }

    @Override
    public void register(Collection<Function<TargetObject, ? extends MethodSequenceFinder>> methodSequenceFinderList) {
        methodSequenceFinderFunctions.addAll(methodSequenceFinderList);
    }

    @Override
    public void register(Function<TargetObject, ? extends MethodSequenceFinder> methodSequenceFinder) {
        methodSequenceFinderFunctions.add(methodSequenceFinder);
    }

    @Override
    public void unregister() {
        methodSequenceFinderFunctions.clear();
    }

    private void findReflectionCallsRecursive(@NotNull TargetObject targetObject, History<Executable> history) {
        findCallsRecursiveBase(targetObject, (m, o) -> tryFindReflectionCalls(o, history, m));
    }

    private void findJacoDBCallsRecursive(@NotNull TargetObject targetObject, History<JcMethod> history) {
        findCallsRecursiveBase(targetObject, (m, o) -> tryFindJacoDBCalls(o, history, m));
    }

    private void findCallsRecursiveBase(@NotNull TargetObject targetObject, BiConsumer<MethodSequenceFinder, TargetObject> consumer) {
        if (targetObject.getObject() == null) {
            consumer.accept(nullMethodSequenceFinder, targetObject);
            return;
        }
        Class<?> clazz = targetObject.getClazz();
        MethodSequenceFinder methodSequenceFinder = cachedFinders.get(clazz);
        if (methodSequenceFinder != null) {
            consumer.accept(methodSequenceFinder, targetObject);
            return;
        }
        for (Function<TargetObject, ? extends MethodSequenceFinder> function : methodSequenceFinderFunctions) {
            try {
                methodSequenceFinder = function.apply(targetObject);
                if (methodSequenceFinder.canTry(targetObject)) {
                    consumer.accept(methodSequenceFinder, targetObject);
                    cachedFinders.put(clazz, methodSequenceFinder);
                    return;
                }
            } catch (Exception ignored) {
                logging(methodSequenceFinder);
            }
        }
        throw new MethodSequenceNotFoundException();
    }


    private void tryFindReflectionCalls(
            TargetObject targetObject,
            History<Executable> history,
            @NotNull MethodSequenceFinder methodSequenceFinder
    ) {
        ResultFinding resultFinding = methodSequenceFinder.findReflectionCallsInternal(targetObject, history);
        List<Object> suspects = resultFinding.getSuspects();
        suspects.forEach(it -> findReflectionCallsRecursive(new TargetObject(it), history));
        if (!resultFinding.isSuccess()) {
            useReflection(targetObject, new TargetObject(resultFinding.getActualObject()), history)
                    .getSuspects().forEach(it -> findReflectionCallsRecursive(new TargetObject(it), history));
        }
    }

    private void tryFindJacoDBCalls(
            TargetObject targetObject,
            History<JcMethod> history,
            @NotNull MethodSequenceFinder methodSequenceFinder
    ) {
        ResultFinding resultFinding = methodSequenceFinder.findJacoDBCallsInternal(targetObject, history);
        List<Object> suspects = resultFinding.getSuspects();
        suspects.forEach(it -> findJacoDBCallsRecursive(new TargetObject(it), history));
        if (!resultFinding.isSuccess()) {
            useReflection(targetObject, new TargetObject(resultFinding.getActualObject()), history)
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

    private static void logging(@Nullable MethodSequenceFinder finder) {
        if (finder != null) {
            String className = finder.getClass().getName();
            String msg = String.format("%s failed", className);
            LOGGER.warning(msg);
        }
    }
}
