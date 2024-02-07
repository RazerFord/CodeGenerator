package org.codegenerator.generator.methodsequencefinders.internal;

import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryArray;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayMethodSequenceFinder implements MethodSequenceFinderInternal {
    @Override
    public boolean canTry(@NotNull Object object) {
        return object.getClass().isArray();
    }

    @Override
    public List<Buildable> findBuildableList(@NotNull Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> findReflectionCallsInternal(@NotNull Object object, @NotNull History<Executable> history) {
        return findCallsInternal(object, history);
    }

    @Override
    public List<Object> findJacoDBCallsInternal(@NotNull Object object, @NotNull History<JcMethod> history) {
        return findCallsInternal(object, history);
    }

    private <T> @NotNull List<Object> findCallsInternal(@NotNull Object object, @NotNull History<T> history) {
        history.put(object, new HistoryArray<>(object, Collections.emptyList()));
        List<Object> suspects = new ArrayList<>();
        arrayTraversal(object, suspects);
        return suspects;
    }

    private static void arrayTraversal(@NotNull Object object, List<Object> suspects) {
        if (!object.getClass().isArray()) {
            suspects.add(object);
        } else {
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                arrayTraversal(Array.get(object, i), suspects);
            }
        }
    }
}
