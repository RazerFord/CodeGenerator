package org.codegenerator.history;

import java.util.List;

public interface HistoryNode<T> {
    Object getObject();

    List<HistoryCall<T>> getHistoryCalls();

    HistoryType getType();
}
