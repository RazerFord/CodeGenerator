package org.codegenerator.generator.methodsequencefinders.internal;

import org.apache.commons.lang3.NotImplementedException;
import org.codegenerator.Utils;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFinding;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFindingImpl;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryNode;
import org.codegenerator.history.HistoryObject;
import org.codegenerator.history.SetterUsingReflection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

public class ReflectionMethodSequenceFinder {
    private final Map<Class<?>, List<Field>> cachedFields = new IdentityHashMap<>();

    public List<Buildable> findBuildableList(@NotNull Object expected, @NotNull Object actual) {
        throw new NotImplementedException();
    }

    public <T> ResultFinding findSetter(
            @NotNull Object expected,
            @NotNull Object actual,
            History<T> history
    ) {
        Class<?> expectedClass = expected.getClass();
        Class<?> actualClass = actual.getClass();

        if (expectedClass != actualClass) {
            throw new IllegalArgumentException();
        }

        List<Field> fields = cachedFields.computeIfAbsent(expectedClass, this::getFields);

        List<Object> suspects = Utils.callSupplierWrapper(() -> trySetFieldsAndUpdateHistory(fields, expected, actual, history));

        return new ResultFindingImpl(actual, 0, suspects);
    }

    @Contract(pure = true)
    private <T> @NotNull List<Object> trySetFieldsAndUpdateHistory(
            @NotNull List<Field> fields,
            Object expected,
            Object actual,
            History<T> history
    ) throws IllegalAccessException {
        List<Object> suspects = new ArrayList<>();
        List<SetterUsingReflection<T>> sur = new ArrayList<>();
        for (Field field : fields) {
            Object expectedValue = field.get(expected);
            Object actualValue = field.get(actual);
            if (!equals(expectedValue, actualValue)) {
                field.set(actual, expectedValue);
                suspects.add(expectedValue);
                sur.add(new SetterUsingReflection<>(history, field, expectedValue));
            }
        }
        HistoryNode<T> old = history.get(expected);
        history.put(expected, new HistoryObject<>(expected, old.getHistoryCalls(), sur));
        return suspects;
    }

    private @NotNull List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            extractFields(clazz, fields);
        }
        return fields;
    }

    private void extractFields(@NotNull Class<?> clazz, List<Field> fields) {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            fields.add(field);
        }
    }

    private boolean equals(Object o1, Object o2) {
        return Objects.deepEquals(o1, o2);
    }
}
