package org.codegenerator.generator.methodsequencefinders;

import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Call;
import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryObject;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;

public class PipelineMethodSequenceFinder implements MethodSequenceFinder {
    private final List<? extends MethodSequenceFinderInternal> methodSequenceFinderList;

    public PipelineMethodSequenceFinder(List<? extends MethodSequenceFinderInternal> methodSequenceFinderList) {
        this.methodSequenceFinderList = methodSequenceFinderList;
    }

    @Override
    public List<Buildable> findBuildableList(@NotNull Object finalObject) {
        for (MethodSequenceFinder methodSequenceFinder : methodSequenceFinderList) {
            try {
                return methodSequenceFinder.findBuildableList(finalObject);
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
    public List<Call<JcMethod>> findJacoDBCalls(@NotNull Object finalObject) {
        for (MethodSequenceFinder methodSequenceFinder : methodSequenceFinderList) {
            try {
                return methodSequenceFinder.findJacoDBCalls(finalObject);
            } catch (Exception ignored) {
                // this code block is empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }

    private void findReflectionCallsRecursive(@NotNull Object object, History<Executable> history) {
        Class<?> clazz = object.getClass();
        if (ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class) {
            history.put(object, new HistoryObject<>(object, Collections.emptyList()));
        } else if (clazz.isArray()) {
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                findReflectionCallsRecursive(Array.get(object, i), history);
            }
        } else {
            for (MethodSequenceFinderInternal methodSequenceFinder : methodSequenceFinderList) {
                try {
                    List<Object> suspects = methodSequenceFinder.findReflectionCallsInternal(object, history);
                    suspects.forEach(it -> findReflectionCallsRecursive(it, history));
                    return;
                } catch (Exception ignored) {
                    // this code block is empty
                }
            }
            throw new MethodSequenceNotFoundException();
        }
    }
}
