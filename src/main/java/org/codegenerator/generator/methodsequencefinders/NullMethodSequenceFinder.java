package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryObject;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;

public class NullMethodSequenceFinder implements MethodSequenceFinderInternal {
    @Override
    public boolean canTry(Object object) {
        return object == null;
    }

    @Override
    public List<Buildable> findBuildableList(@Nullable Object object) {
        return Collections.emptyList();
    }

    @Override
    public History<Executable> findReflectionCalls(@Nullable Object object) {
        return findCallsInternal(object);
    }

    @Override
    public History<JcMethod> findJacoDBCalls(@Nullable Object object) {
        return findCallsInternal(object);
    }

    @Override
    public List<Object> findReflectionCallsInternal(@Nullable Object object, @NotNull History<Executable> history) {
        return findCallsInternal(object, history);
    }

    @Override
    public List<Object> findJacoDBCallsInternal(@Nullable Object object, @NotNull History<JcMethod> history) {
        return findCallsInternal(object, history);
    }

    private <T> @NotNull History<T> findCallsInternal(Object object) {
        History<T> history = new History<>();
        history.put(object, new HistoryObject<>(object, Collections.emptyList()));
        return history;
    }

    private <T> @Unmodifiable @NotNull List<Object> findCallsInternal(Object object, @NotNull History<T> history) {
        history.put(object, new HistoryObject<>(object, Collections.emptyList()));
        return Collections.emptyList();
    }
}
