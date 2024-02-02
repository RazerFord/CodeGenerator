package org.codegenerator.extractor;

import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ClassFieldExtractor {
    private ClassFieldExtractor() {
    }

    public static @NotNull Node extract(@NotNull Object o) {
        Map<Object, Node> visited = new HashMap<>();

        Node node = Node.createNode(o, visited);
        visited.put(o, node);
        node.extract();
        return node;
    }
}
