package org.codegenerator.history;

import java.util.IdentityHashMap;
import java.util.Map;

public class History<T> {
    private final Map<Object, HistoryNode<T>> historiesNode = new IdentityHashMap<>();

    public HistoryNode<T> get(Object object) {
        return historiesNode.get(object);
    }

    public HistoryNode<T> put(Object object, HistoryNode<T> historyObject) {
        return historiesNode.put(object, historyObject);
    }
}
