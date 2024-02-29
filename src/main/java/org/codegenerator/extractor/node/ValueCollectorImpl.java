package org.codegenerator.extractor.node;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ValueCollectorImpl implements NodeVisitor {
    private final Set<Object> values = new HashSet<>();

    public Set<Object> getValues() {
        return Collections.unmodifiableSet(values);
    }

    @Override
    public void visit(@NotNull ArrayNode node) {
        values.add(node.getValue());
        for (Node node1 : node.values()) {
            node1.accept(this);
        }
    }

    @Override
    public void visit(@NotNull InnerNode node) {
        values.add(node.getValue());
        for (Node node1 : node.values()) {
            node1.accept(this);
        }
    }

    @Override
    public void visit(@NotNull Leaf node) {
        values.add(node.getValue());
    }
}
