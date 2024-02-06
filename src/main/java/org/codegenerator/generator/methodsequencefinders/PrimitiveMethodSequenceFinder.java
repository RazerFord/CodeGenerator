package org.codegenerator.generator.methodsequencefinders;

import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryPrimitive;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;

public class PrimitiveMethodSequenceFinder implements MethodSequenceFinderInternal {
    @Override
    public boolean canTry(@NotNull Object object) {
        Class<?> clazz = object.getClass();
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class;
    }

    @Override
    public List<Buildable> findBuildableList(@NotNull Object object) {
        return Collections.emptyList();
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
        history.put(object, new HistoryPrimitive<>(object, Collections.emptyList()));
        return Collections.emptyList();
    }

    @Override
    public List<Object> findJacoDBCallsInternal(@NotNull Object object, @NotNull History<JcMethod> history) {
        history.put(object, new HistoryPrimitive<>(object, Collections.emptyList()));
        return Collections.emptyList();
    }

    private <T> @NotNull History<T> findCallsInternal(@NotNull Object object) {
        History<T> history = new History<>();
        history.put(object, new HistoryPrimitive<>(object, Collections.emptyList()));
        return history;
    }
}
