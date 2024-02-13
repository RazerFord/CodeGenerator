package org.codegenerator.history;

import java.lang.reflect.Field;

public class SetterUsingReflection<T> {
    private final History<T> history;
    private final Field field;
    private final Object setObject;

    public SetterUsingReflection(History<T> history, Field field, Object setObject) {
        this.history = history;
        this.field = field;
        this.setObject = setObject;
    }

    public Field getField() {
        return field;
    }

    public Object getSetObject() {
        return setObject;
    }

    public HistoryNode<T> getHistoryArg() {
        return history.get(setObject);
    }
}
