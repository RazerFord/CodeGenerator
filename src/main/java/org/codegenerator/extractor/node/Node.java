package org.codegenerator.extractor.node;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public interface Node extends Map<Object, Node> {
    Class<?> getClassOfValue();

    Object getValue();

    void extract();

    NodeType nodeType();

    int power();

    int diff(Node that);

    enum NodeType {
        ARRAY, INNER, LEAF,
    }

    static @NotNull Node createNode(Object o) {
        Map<Object, Node> visited = new HashMap<>();

        return NodeUtils.createNode(o, visited);
    }
}
