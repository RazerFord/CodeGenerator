package org.codegenerator.history;


import java.util.List;

public class HistoryArray<T> implements HistoryNode<T> {
    private final Object object;
    private final List<HistoryCall<T>> historyCalls;

    public HistoryArray(Object object, List<HistoryCall<T>> historyCalls) {
        this.object = object;
        this.historyCalls = historyCalls;
    }

    public Object getObject() {
        return object;
    }

    public List<HistoryCall<T>> getHistoryCalls() {
        return historyCalls;
    }

    @Override
    public HistoryType getType() {
        return HistoryType.ARRAY;
    }
}