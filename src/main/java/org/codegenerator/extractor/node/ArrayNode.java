package org.codegenerator.extractor.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;

import static org.codegenerator.Utils.throwUnless;

public class ArrayNode implements Node {
    private final Class<?> clazz;
    private final Object[] value;
    private final Map<Object, Node> fields = new HashMap<>();
    private final Map<Object, Node> visited;
    private final Supplier<Integer> power;

    ArrayNode(@NotNull Class<?> clazz, Object value, Map<Object, Node> visited) {
        this.clazz = clazz;
        throwUnless(clazz.isArray(), new IllegalArgumentException());
        int length = Array.getLength(value);
        this.value = new Object[length];
        for (int i = 0; i < length; i++) {
            this.value[i] = Array.get(value, i);
        }
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
        for (int i = 0; i < value.length; i++) {
            Node node = visited.get(value[i]);
            if (node != null) {
                fields.put(i, node);
            } else {
                Class<?> componentType = value[i] != null ?
                        value[i].getClass() :
                        clazz.getComponentType();
                node = NodeUtils.createNode(componentType, value[i], visited);
                fields.put(i, node);
            }
        }
    }

    @Override
    public NodeType nodeType() {
        return NodeType.ARRAY;
    }

    @Override
    public int power() {
        return power.get();
    }

    @Override
    public int diff(Node that) {
        if (!(that instanceof ArrayNode)) return power();
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
    public Node put(Object key, Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NotNull Map<?, ? extends Node> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<Object> keySet() {
        return fields.keySet();
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
        if (!(o instanceof ArrayNode)) return false;
        ArrayNode arrayNode = (ArrayNode) o;
        return Arrays.equals(arrayNode.value, value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, Arrays.hashCode(value));
    }
}
