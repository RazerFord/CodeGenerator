package org.codegenerator.extractor.node;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public class NodeUtils {
    private NodeUtils() {
    }

    static int diff(Node l, Node r) {
        if (l == null) {
            return r == null ? 0 : r.power();
        }
        return l.diff(r);
    }

    static @NotNull Node createNode(@NotNull Class<?> clazz, Object o, Map<Object, Node> visited) {
        if (o == null) {
            return Leaf.NULL_NODE;
        }
        if (ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class) {
            return new Leaf(o.getClass(), o);
        }
        if (clazz.isArray()) {
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
            return new Leaf(clz, o);
        }
        if (clz.isArray()) {
            return new ArrayNode(clz, o, visited);
        }
        return new InnerNode(clz, o, visited);
    }

    @Contract(pure = true)
    static @NotNull Supplier<Integer> createPowerSupplier(Map<Object, Node> map) {
        return new Object() {
            Supplier<Integer> supplier;

            {
                supplier = () -> {
                    int val = map.values().stream().mapToInt(Node::power).sum();
                    supplier = () -> val;
                    return val;
                };
            }
        }.supplier;
    }
}
