package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.List;

public class PipelineMethodSequenceFinder implements MethodSequenceFinder {
    private final List<? extends MethodSequenceFinderInternal> methodSequenceFinderList;

    public PipelineMethodSequenceFinder(List<? extends MethodSequenceFinderInternal> methodSequenceFinderList) {
        this.methodSequenceFinderList = methodSequenceFinderList;
    }

    @Override
    public boolean canTry(Object object) {
        return true;
    }

    @Override
    public List<Buildable> findBuildableList(@NotNull Object object) {
        for (MethodSequenceFinder methodSequenceFinder : methodSequenceFinderList) {
            try {
                return methodSequenceFinder.findBuildableList(object);
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

    private void findReflectionCallsRecursive(@NotNull Object object, History<Executable> history) {
        for (MethodSequenceFinderInternal methodSequenceFinder : methodSequenceFinderList) {
            try {
                if (methodSequenceFinder.canTry(object)) {
                    List<Object> suspects = methodSequenceFinder.findReflectionCallsInternal(object, history);
                    suspects.forEach(it -> findReflectionCallsRecursive(it, history));
                    return;
                }
            } catch (Exception ignored) {
                // this code block is empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }

    private void findJacoDBCallsRecursive(@NotNull Object object, History<JcMethod> history) {
        for (MethodSequenceFinderInternal methodSequenceFinder : methodSequenceFinderList) {
            try {
                if (methodSequenceFinder.canTry(object)) {
                    List<Object> suspects = methodSequenceFinder.findJacoDBCallsInternal(object, history);
                    suspects.forEach(it -> findJacoDBCallsRecursive(it, history));
                    return;
                }
            } catch (Exception ignored) {
                // this code block is empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }
}
