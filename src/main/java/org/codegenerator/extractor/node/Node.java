package org.codegenerator.extractor.node;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public interface Node extends Map<Object, Node> {
    Class<?> getClassOfValue();

    Object getValue();

    NodeType nodeType();

    int power();

    int diff(Node that);

    void accept(NodeVisitor visitor);

    @Override
    default Node put(Object field, Node node) {
        throw new UnsupportedOperationException("put");
    }

    @Override
    default Node remove(Object o) {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    default void putAll(@NotNull Map<?, ? extends Node> map) {
        throw new UnsupportedOperationException("putAll");
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException("clear");
    }

    enum NodeType {
        ARRAY, INNER, LEAF,
    }

    static @NotNull Node createNode(Object o) {
        Map<Object, Node> visited = new HashMap<>();

        return NodeUtils.createNode(o, visited);
    }
}
