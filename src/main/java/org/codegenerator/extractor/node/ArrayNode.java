package org.codegenerator.extractor.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

public class ArrayNode implements Node {
    private final Class<?> clazz;
    private final Object[] value;
    private final Map<Integer, Node> fields = new HashMap<>();
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
    public void extract() throws IllegalAccessException {
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
        if (!(that instanceof ArrayNode)) return -1;
        int diff = 0;
        for (Map.Entry<Integer, Node> entry : fields.entrySet()) {
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
        if (key instanceof Integer) {
            return fields.put((Integer) key, node);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Node remove(Object o) {
        return fields.remove(o);
    }

    @Override
    public void putAll(@NotNull Map<?, ? extends Node> map) {
        for (Entry<?, ? extends Node> e : map.entrySet()) {
            if (e.getKey() instanceof Integer) {
                fields.put((Integer) e.getKey(), e.getValue());
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
        return ((new HashMap<Object, Node>(fields)).entrySet());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArrayNode)) return false;
        return entrySet().equals(((Node) o).entrySet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, Arrays.hashCode(value));
    }
}
