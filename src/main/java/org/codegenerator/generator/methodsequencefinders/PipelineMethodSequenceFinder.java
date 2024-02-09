package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.internal.MethodSequenceFinderInternal;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFinding;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PipelineMethodSequenceFinder implements MethodSequenceFinder {
    private final Map<Class<?>, MethodSequenceFinderInternal> cachedFinders = new IdentityHashMap<>();
    private final List<Function<Object, ? extends MethodSequenceFinderInternal>> methodSequenceFinderFunctions;

    public PipelineMethodSequenceFinder(List<Function<Object, ? extends MethodSequenceFinderInternal>> methodSequenceFinderFunctions) {
        this.methodSequenceFinderFunctions = methodSequenceFinderFunctions;
    }

    @Override
    public List<Buildable> findBuildableList(@NotNull Object object) {
        Class<?> clazz = object.getClass();
        MethodSequenceFinderInternal methodSequenceFinder = cachedFinders.get(clazz);
        if (methodSequenceFinder != null) return methodSequenceFinder.findBuildableList(object);
        for (Function<Object, ? extends MethodSequenceFinderInternal> function : methodSequenceFinderFunctions) {
            try {
                methodSequenceFinder = function.apply(object);
                if (methodSequenceFinder.canTry(object)) {
                    List<Buildable> buildableList = methodSequenceFinder.findBuildableList(object);
                    cachedFinders.put(clazz, methodSequenceFinder);
                    return buildableList;
                }
            } catch (Exception ignored) {
                // this code block is empty
            }
        }
        throw new MethodSequenceNotFoundException();
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
        Class<?> clazz = object.getClass();
        MethodSequenceFinderInternal methodSequenceFinder = cachedFinders.get(clazz);
        if (methodSequenceFinder != null) {
            ResultFinding resultFinding = methodSequenceFinder.findReflectionCallsInternal(object, history);
            List<Object> suspects = resultFinding.getSuspects();
            suspects.forEach(it -> findReflectionCallsRecursive(it, history));
            return;
        }
        for (Function<Object, ? extends MethodSequenceFinderInternal> function : methodSequenceFinderFunctions) {
            try {
                methodSequenceFinder = function.apply(object);
                if (methodSequenceFinder.canTry(object)) {
                    ResultFinding resultFinding = methodSequenceFinder.findReflectionCallsInternal(object, history);
                    List<Object> suspects = resultFinding.getSuspects();
                    suspects.forEach(it -> findReflectionCallsRecursive(it, history));
                    cachedFinders.put(clazz, methodSequenceFinder);
                    return;
                }
            } catch (Exception ignored) {
                // this code block is empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }

    private void findJacoDBCallsRecursive(@NotNull Object object, History<JcMethod> history) {
        Class<?> clazz = object.getClass();
        MethodSequenceFinderInternal methodSequenceFinder = cachedFinders.get(clazz);
        if (methodSequenceFinder != null) {
            ResultFinding resultFinding = methodSequenceFinder.findJacoDBCallsInternal(object, history);
            List<Object> suspects = resultFinding.getSuspects();
            suspects.forEach(it -> findJacoDBCallsRecursive(it, history));
            return;
        }
        for (Function<Object, ? extends MethodSequenceFinderInternal> function : methodSequenceFinderFunctions) {
            try {
                methodSequenceFinder = function.apply(object);
                if (methodSequenceFinder.canTry(object)) {
                    ResultFinding resultFinding = methodSequenceFinder.findJacoDBCallsInternal(object, history);
                    List<Object> suspects = resultFinding.getSuspects();
                    suspects.forEach(it -> findJacoDBCallsRecursive(it, history));
                    cachedFinders.put(clazz, methodSequenceFinder);
                    return;
                }
            } catch (Exception ignored) {
                // this code block is empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }
}
