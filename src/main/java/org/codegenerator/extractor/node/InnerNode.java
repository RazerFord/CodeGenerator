package org.codegenerator.extractor.node;

import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Utils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

import static org.codegenerator.Utils.throwIf;

public class InnerNode implements Node {
    private boolean visited = false;
    private final Class<?> clazz;
    private final Object value;
    private final Map<Object, Node> fields;
    private final Supplier<Integer> power;

    InnerNode(@NotNull Class<?> clazz, Object value, @NotNull Map<Object, Node> visited) {
        Supplier<? extends RuntimeException> supplier = () -> new IllegalArgumentException("InnerNode");
        throwIf(clazz.isArray() || ClassUtils.isPrimitiveOrWrapper(clazz), supplier);

        this.clazz = clazz;
        this.value = value;

        visited.put(value, this);
        fields = Collections.unmodifiableMap(extract(visited));
        power = NodeUtils.createPowerSupplier(fields);
    }

    @Override
    public Class<?> getClassOfValue() {
        return clazz;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NodeType nodeType() {
        return NodeType.INNER;
    }

    @Override
    public int power() {
        return power.get();
    }

    @Override
    public int diff(Node that) {
        if (!(that instanceof InnerNode)) return power();
        int diff = 0;
        for (Map.Entry<Object, Node> entry : fields.entrySet()) {
            int curDiff = NodeUtils.diff(entry.getValue(), that.get(entry.getKey()));
            // diff + curDiff >= MAX => curDiff >= MAX - diff
            if (curDiff >= Integer.MAX_VALUE - diff) return Integer.MAX_VALUE;
            diff += curDiff;
        }
        return diff;
    }

    @Override
    public void accept(@NotNull NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int size() {
        return fields.size();
    }

    @Override
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return fields.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return fields.containsValue(o);
    }

    @Override
    public Node get(Object o) {
        return fields.get(o);
    }

    @NotNull
    @Override
    public Set<Object> keySet() {
        return new HashSet<>(fields.keySet());
    }

    @NotNull
    @Override
    public Collection<Node> values() {
        return fields.values();
    }

    @NotNull
    @Override
    public Set<Entry<Object, Node>> entrySet() {
        return fields.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InnerNode)) return false;
        // If recursion is detected, true should be returned.
        // Since recursion could occur if `equals` of objects returned true
        if (visited) return true;
        visited = true;
        InnerNode innerNode = (InnerNode) o;
        boolean result = Objects.equals(value, innerNode.value) && innerNode.fields.equals(fields);
        visited = false;
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, value, fields.keySet());
    }

    private @NotNull Map<Object, Node> extract(Map<Object, Node> visited) {
        Map<Object, Node> map = new HashMap<>();
        Class<?> clz = clazz;

        while (clz != null) {
            for (Field field : clz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;

                field.setAccessible(true);
                Object o = Utils.callSupplierWrapper(() -> field.get(value));

                Node node = visited.get(o);
                if (node != null) {
                    map.put(field, node);
                } else {
                    Class<?> type = field.getType();
                    if (field.getGenericType() == type) node = NodeUtils.createNode(type, o, visited);
                    else node = NodeUtils.createNode(o, visited);
                    map.put(field, node);
                }
            }
            clz = clz.getSuperclass();
        }
        return map;
    }
}
