package org.codegenerator.history;

import java.util.HashMap;
import java.util.Map;

/**
 * The History class stores information about the methods
 * that might have been called to obtain the requested object.
 *
 * @param <T> type of methods in {@link  HistoryCall<T>}
 */
public class History<T> {
    private final Map<Object, HistoryNode<T>> historiesNode = new HashMap<>();

    /**
     * Returns the {@link HistoryNode<T>} for the requested object.
     *
     * @param object for which you need to get a list of methods
     * @return {@link HistoryNode<T>}
     */
    public HistoryNode<T> get(Object object) {
        return historiesNode.get(object);
    }

    /**
     * Puts the {@link HistoryNode<T>} for the {@code object}.
     *
     * @param object        for which you want to put a list of methods
     * @param historyObject {@link HistoryNode<T>}
     * @return {@link HistoryNode<T>}
     */
    public HistoryNode<T> put(Object object, HistoryNode<T> historyObject) {
        return historiesNode.put(object, historyObject);
    }
}
