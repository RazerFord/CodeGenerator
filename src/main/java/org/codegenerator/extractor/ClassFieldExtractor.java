package org.codegenerator.extractor;

import org.codegenerator.extractor.node.InnerNode;
import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ClassFieldExtractor {
    public static @NotNull Node extract(@NotNull Object o) {
        Map<Object, Node> visited = new HashMap<>();

        Node node = Node.createNode(o, visited);
        visited.put(o, node);
        try {
            node.extract();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return node;
    }
}
