package org.codegenerator.extractor.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

public class ArrayNode implements Node {
    private final Class<?> clazz;
    private final Object[] value;
    private final Map<Object, Node> fields = new HashMap<>();
    private final Map<Object, Node> visited;

    public ArrayNode(@NotNull Class<?> clazz, Object value, Map<Object, Node> visited) {
        this.clazz = clazz;
        if (!clazz.isArray()) throw new IllegalArgumentException();
        int length = Array.getLength(value);
        Object[] newValue = new Object[length];
        for (int i = 0; i < length; i++) {
            newValue[i] = Array.get(value, i);
        }
        this.value = newValue;
        this.visited = visited;
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
            if (visited.containsKey(value[i])) {
                fields.put(i, visited.get(value[i]));
            } else {
                Node node = Node.createNode(value[i], visited);
                fields.put(i, node);
                node.extract();
            }
        }
    }

    @Override
    public NodeType nodeType() {
        return NodeType.ARRAY;
    }

    @Override
    public int diff(Node that) {
        if (!(that instanceof ArrayNode)) return Integer.MAX_VALUE;
        int diff = 0;
        for (Map.Entry<Object, Node> entry : fields.entrySet()) {
            if (!Objects.equals(that.get(entry.getKey()), entry.getValue())) {
                diff++;
            }
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
