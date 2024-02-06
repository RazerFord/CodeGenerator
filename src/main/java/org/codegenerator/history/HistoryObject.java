package org.codegenerator.history;

import java.util.List;

public class HistoryObject<T> implements HistoryNode<T> {
    private final Object object;
    private final List<HistoryCall<T>> historyCalls;

    public HistoryObject(Object object, List<HistoryCall<T>> historyCalls) {
        this.object = object;
        this.historyCalls = historyCalls;
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
    public HistoryType getType() {
        return HistoryType.OBJECT;
    }
}
