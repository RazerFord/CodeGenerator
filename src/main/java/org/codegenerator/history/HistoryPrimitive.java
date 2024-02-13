package org.codegenerator.history;

import java.util.Collections;
import java.util.List;

public class HistoryPrimitive<T> implements HistoryNode<T> {
    private final Object object;
    private final List<HistoryCall<T>> historyCalls;
    private final Class<?> creator;

    public HistoryPrimitive(
            Object object,
            List<HistoryCall<T>> historyCalls,
            Class<?> creator
    ) {
        this.object = object;
        this.historyCalls = historyCalls;
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
        return Collections.emptyList();
    }

    @Override
    public HistoryType getType() {
        return HistoryType.PRIMITIVE;
    }

    @Override
    public Class<?> getCreatorType() {
        return creator;
    }
}
