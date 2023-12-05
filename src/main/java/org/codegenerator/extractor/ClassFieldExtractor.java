package org.codegenerator.extractor;

import org.codegenerator.extractor.node.InnerNode;
import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.NotNull;

public class ClassFieldExtractor {
    public Node extract(@NotNull Object o) {
        Node node = new InnerNode(o.getClass(), o);
        try {
            node.extract();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return node;
    }
}
