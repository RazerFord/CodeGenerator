package org.codegenerator.history;

import java.util.IdentityHashMap;
import java.util.Map;

public class History<T> {
    private final Map<Object, HistoryObject<T>> historiesObject = new IdentityHashMap<>();

    public HistoryObject<T> get(Object object) {
        return historiesObject.get(object);
    }

    public HistoryObject<T> put(Object object, HistoryObject<T> historyObject) {
        return historiesObject.put(object, historyObject);
    }
}
