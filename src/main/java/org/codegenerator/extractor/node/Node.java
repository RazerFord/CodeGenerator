package org.codegenerator.extractor.node;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

public interface Node extends Map<Object, Node> {
    Class<?> getClassOfValue();

    Object getValue();

    void extract() throws IllegalAccessException;

    NodeType nodeType();

    int diff(Node that);

    enum NodeType {
        ARRAY,
        INNER,
        LEAF,
    }

    static @NotNull Node createNode(@NotNull Field field, Object o, Map<Object, Node> visited) {
        if (o == null) {
            return Leaf.NULL_NODE;
        }
        Class<?> clz = field.getType();
        if (ClassUtils.isPrimitiveOrWrapper(field.getType()) || clz == String.class) {
            return new Leaf(o.getClass(), o, visited);
        }
        if (field.getType().isArray()) {
            return new ArrayNode(o.getClass(), o, visited);
        }
        return new InnerNode(o.getClass(), o, visited);
    }

    static @NotNull Node createNode(Object o, Map<Object, Node> visited) {
        if (o == null) {
            return Leaf.NULL_NODE;
        }
        Class<?> clz = o.getClass();
        if (ClassUtils.isPrimitiveOrWrapper(clz) || clz == String.class) {
            return new Leaf(clz, o, visited);
        }
        if (clz.isArray()) {
            return new ArrayNode(clz, o, visited);
        }
        return new InnerNode(clz, o, visited);
    }
}
