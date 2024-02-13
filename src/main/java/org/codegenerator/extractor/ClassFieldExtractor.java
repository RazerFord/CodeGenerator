package org.codegenerator.extractor;

import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.NotNull;

public class ClassFieldExtractor {
    private ClassFieldExtractor() {
    }

    public static @NotNull Node extract(@NotNull Object o) {
        return Node.createNode(o);
    }
}
