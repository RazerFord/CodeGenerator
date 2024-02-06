package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryArray;
import org.codegenerator.history.HistoryObject;
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
    public History<Executable> findReflectionCalls(@NotNull Object object) {
        return findCallsInternal(object);
    }

    @Override
    public History<JcMethod> findJacoDBCalls(@NotNull Object object) {
        return findCallsInternal(object);
    }

    @Override
    public List<Object> findReflectionCallsInternal(@NotNull Object object, @NotNull History<Executable> history) {
        return findCallsInternal(object, history);
    }

    @Override
    public List<Object> findJacoDBCallsInternal(@NotNull Object object, @NotNull History<JcMethod> history) {
        return findCallsInternal(object, history);
    }

    private <T> @NotNull History<T> findCallsInternal(@NotNull Object object) {
        History<T> history = new History<>();
        history.put(object, new HistoryObject<>(object, Collections.emptyList()));
        return history;
    }

    private <T> @NotNull List<Object> findCallsInternal(@NotNull Object object, @NotNull History<T> history) {
        int length = Array.getLength(object);
        List<Object> suspects = new ArrayList<>(length);
        history.put(object, new HistoryArray<>(object, Collections.emptyList()));
        for (int i = 0; i < length; i++) {
            suspects.add(Array.get(object, i));
        }
        return suspects;
    }
}
