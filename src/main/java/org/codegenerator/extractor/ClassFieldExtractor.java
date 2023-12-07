package org.codegenerator.extractor;

import org.codegenerator.extractor.node.InnerNode;
import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ClassFieldExtractor {
    public Node extract(@NotNull Object o) {
        Set<Object> visited = new HashSet<>();

        Node node = new InnerNode(o.getClass(), o, visited);
        try {
            node.extract();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return node;
    }
}
