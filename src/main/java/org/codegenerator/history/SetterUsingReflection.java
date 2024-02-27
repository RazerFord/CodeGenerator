package org.codegenerator.history;

import java.lang.reflect.Field;

/**
 * Wrapper over a setter that uses Reflection.
 *
 * @param <T> type of methods in {@link  HistoryCall<T>}
 */
public class SetterUsingReflection<T> {
    private final History<T> history;
    private final Field field;
    private final Object setObject;

    public SetterUsingReflection(History<T> history, Field field, Object setObject) {
        this.history = history;
        this.field = field;
        this.setObject = setObject;
    }

    /**
     * Returns a field.
     *
     * @return field
     */
    public Field getField() {
        return field;
    }

    /**
     * Returns the object on which the field is set.
     *
     * @return object on which the field is set
     */
    public Object getSetObject() {
        return setObject;
    }

    /**
     * Returns the {@link HistoryNode<T>} for setObject.
     *
     * @return {@link HistoryNode<T>} for setObject
     */
    public HistoryNode<T> getHistoryArg() {
        return history.get(setObject);
    }
}
