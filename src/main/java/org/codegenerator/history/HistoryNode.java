package org.codegenerator.history;

import org.codegenerator.ItemType;

import java.util.List;

public interface HistoryNode<T> {
    Object getObject();

    List<HistoryCall<T>> getHistoryCalls();

    List<SetterUsingReflection<T>> getSetterUsingReflections();

    ItemType getType();

    Class<?> getCreatorType();

    boolean isGeneric();
}
