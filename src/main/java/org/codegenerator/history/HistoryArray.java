package org.codegenerator.history;


import java.util.List;

public class HistoryArray<T> implements HistoryNode<T> {
    private final Object[] object;
    private final List<HistoryCall<T>> historyCalls;
    private final History<T> history;

    public HistoryArray(Object[] object, List<HistoryCall<T>> historyCalls, History<T> history) {
        this.object = object;
        this.historyCalls = historyCalls;
        this.history = history;
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