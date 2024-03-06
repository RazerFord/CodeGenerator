package org.codegenerator.history;

import java.util.Collections;
import java.util.List;

public class HistoryObject<T> implements HistoryNode<T> {
    private final Object object;
    private final List<HistoryCall<T>> historyCalls;
    private List<SetterUsingReflection<T>> setterUsingReflections;
    private final Class<?> creator;
    private final HistoryNode<T> next;

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
        this(object, historyCalls, setterUsingReflections, creator, null);
    }

    public HistoryObject(
            Object object,
            List<HistoryCall<T>> historyCalls,
            Class<?> creator,
            HistoryNode<T> next
    ) {
        this(object, historyCalls, Collections.emptyList(), creator, next);
    }

    public HistoryObject(
            Object object,
            List<HistoryCall<T>> historyCalls,
            List<SetterUsingReflection<T>> setterUsingReflections,
            Class<?> creator,
            HistoryNode<T> next
    ) {
        this.object = object;
        this.historyCalls = historyCalls;
        this.setterUsingReflections = setterUsingReflections;
        this.creator = creator;
        this.next = next;
    }

    @Override
    public HistoryNode<T> nextNode() {
        return next;
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

    public void setSetterUsingReflections(List<SetterUsingReflection<T>> setterUsingReflections) {
        this.setterUsingReflections = setterUsingReflections;
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
