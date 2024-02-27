package org.codegenerator.history;

import java.util.Collections;
import java.util.List;

public class HistoryObject<T> implements HistoryNode<T> {
    private final Object object;
    private final List<HistoryCall<T>> historyCalls;
    private final List<SetterUsingReflection<T>> setterUsingReflections;
    private final Class<?> creator;

    public HistoryObject(
            Object object,
            List<HistoryCall<T>> historyCalls,
            Class<?> creator
    ) {
        this(object, historyCalls, Collections.emptyList(), creator);
    }

    public HistoryObject(
            Object object,
            List<HistoryCall<T>> historyCalls,
            List<SetterUsingReflection<T>> setterUsingReflections,
            Class<?> creator
    ) {
        this.object = object;
        this.historyCalls = historyCalls;
        this.setterUsingReflections = setterUsingReflections;
        this.creator = creator;
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public List<HistoryCall<T>> getHistoryCalls() {
        return historyCalls;
    }

    @Override
    public List<SetterUsingReflection<T>> getSetterUsingReflections() {
        return setterUsingReflections;
    }

    @Override
    public ItemType getType() {
        return ItemType.OBJECT;
    }

    @Override
    public Class<?> getCreatorType() {
        return creator;
    }

    @Override
    public boolean isGeneric() {
        return object != null && object.getClass().getTypeParameters().length != 0;
    }
}
