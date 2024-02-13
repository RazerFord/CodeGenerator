package org.codegenerator.extractor.node;

import org.codegenerator.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

public class InnerNode implements Node {
    private final Set<Object> visitedDuringEquals = new HashSet<>();
    private final Class<?> clazz;
    private final Object value;
    private final Map<Object, Node> fields = new HashMap<>();
    private final Map<Object, Node> visited;
    private final Supplier<Integer> power;

    InnerNode(Class<?> clazz, Object value, @NotNull Map<Object, Node> visited) {
        this.clazz = clazz;
        this.value = value;
        this.visited = visited;

        visited.put(value, this);
        extract();
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
    public void extract() {
        Class<?> clz = clazz;

        while (clz != null) {
            for (Field field : clz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;

                field.setAccessible(true);
                Object o = Utils.callSupplierWrapper(() -> field.get(value));

                Node node = visited.get(o);
                if (node != null) {
                    fields.put(field, node);
                } else {
                    node = NodeUtils.createNode(field.getType(), o, visited);
                    fields.put(field, node);
                    node.extract();
                }
            }
            clz = clz.getSuperclass();
        }
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

    @Nullable
    @Override
    public Node put(Object field, Node node) {
        if (field instanceof Field) {
            return fields.put(field, node);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Node remove(Object o) {
        return fields.remove(o);
    }

    @Override
    public void putAll(@NotNull Map<?, ? extends Node> map) {
        for (Map.Entry<?, ? extends Node> e : map.entrySet()) {
            if (e.getKey() instanceof Field) {
                fields.put(e.getKey(), e.getValue());
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public void clear() {
        fields.clear();
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
        InnerNode innerNode = (InnerNode) o;
        // If recursion is detected, true should be returned.
        // Since recursion could occur if `equals` of objects returned true
        if (visitedDuringEquals.contains(o)) return true;
        visitedDuringEquals.add(o);
        boolean result = Objects.equals(value, innerNode.value) && innerNode.fields.equals(fields);
        visitedDuringEquals.remove(o);
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, value, fields.keySet());
    }
}
