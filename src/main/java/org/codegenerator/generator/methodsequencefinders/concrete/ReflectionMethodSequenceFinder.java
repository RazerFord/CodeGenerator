package org.codegenerator.generator.methodsequencefinders.concrete;

import org.codegenerator.Utils;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.graph.resultfinding.ResultFindingImpl;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryNode;
import org.codegenerator.history.HistoryObject;
import org.codegenerator.history.SetterUsingReflection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

import static org.codegenerator.Utils.throwIf;

public class ReflectionMethodSequenceFinder {
    private final Map<Class<?>, List<Field>> cachedFields = new IdentityHashMap<>();

    public <T> ResultFinding findSetter(
            @NotNull TargetObject expected,
            @NotNull TargetObject actual,
            History<T> history
    ) {
        return findSetterInternal(expected.getObject(), actual.getObject(), history);
    }

    @Contract("_, _, _ -> new")
    private <T> @NotNull ResultFinding findSetterInternal(
            @NotNull Object expected,
            @NotNull Object actual,
            History<T> history
    ) {
        List<Field> fields = checkAndGetFields(expected, actual);

        List<Object> suspects = new ArrayList<>();
        List<SetterUsingReflection<T>> sur = new ArrayList<>();
        for (Field field : fields) {
            Object expectedValue = Utils.callSupplierWrapper(() -> field.get(expected));
            Object actualValue = Utils.callSupplierWrapper(() -> field.get(actual));
            if (!equals(expectedValue, actualValue)) {
                Utils.callRunnableWrapper(() -> field.set(actual, expectedValue));
                suspects.add(expectedValue);
                sur.add(new SetterUsingReflection<>(history, field, expectedValue));
            }
        }
        updateHistory(history, expected, sur);

        return new ResultFindingImpl(actual, 0, suspects);
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

    private <T> void updateHistory(
            @NotNull History<T> history,
            Object expected,
            List<SetterUsingReflection<T>> sur
    ) {
        HistoryNode<T> old = history.get(expected);
        history.put(expected, new HistoryObject<>(expected, old.getHistoryCalls(), sur, old.getCreatorType(), old.nextNode()));
    }

    private List<Field> checkAndGetFields(@NotNull Object expected, @NotNull Object actual) {
        Class<?> expectedClass = expected.getClass();
        Class<?> actualClass = actual.getClass();

        throwIf(expectedClass != actualClass, new IllegalArgumentException());

        return cachedFields.computeIfAbsent(expectedClass, this::getFields);
    }

    private boolean equals(Object o1, Object o2) {
        return Objects.deepEquals(o1, o2);
    }
}
