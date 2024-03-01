package org.codegenerator.extractor.node;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ValueCollectorImpl implements NodeVisitor {
    private final Map<Class<?>, List<Object>> typeToValues = new HashMap<>();

    public Map<Class<?>, List<Object>> getTypeToValues() {
        return Collections.unmodifiableMap(typeToValues);
    }

    @Override
    public void visit(@NotNull ArrayNode node) {
        if (node.getValue() == null) return;

        Class<?> componentType = node.getClassOfValue().getComponentType();
        for (Node node1 : node.values()) {
            typeToValues
                    .computeIfAbsent(componentType, k -> new ArrayList<>())
                    .add(node1.getValue());
            node1.accept(this);
        }
    }

    @Override
    public void visit(@NotNull InnerNode node) {
        for (Map.Entry<Object, Node> e : node.entrySet()) {
            Node node1 = e.getValue();
            Object value = node1.getValue();
            if (value == null) continue;

            Class<?> type = ClassUtils.primitiveToWrapper(node1.getClassOfValue());
            typeToValues
                    .computeIfAbsent(type, k -> new ArrayList<>())
                    .add(value);
            node1.accept(this);
        }
    }

    @Override
    public void visit(@NotNull Leaf node) {
        // this block of code must be empty
    }
}
