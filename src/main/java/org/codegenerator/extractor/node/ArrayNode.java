package org.codegenerator.extractor.node;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class ArrayNode implements Node {
    private final Class<?> clazz;
    private final Object value;
    private final Map<Integer, Node> fields = new HashMap<>();
    private final Map<Object, Node> visited;

    public ArrayNode(Class<?> clazz, Object value, Map<Object, Node> visited) {
        this.clazz = clazz;
        this.value = value;
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
        // TODO
    }

    @Override
    public NodeType nodeType() {
        return NodeType.INNER;
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

    @Contract("_, _ -> new")
    private @NotNull Node createNode(@NotNull Field field, Object o) {
        if (o == null) {
            return Leaf.NULL_NODE;
        }
        if (field.getType().isPrimitive()) {
            return new Leaf(o.getClass(), o, visited);
        }
        return new ArrayNode(o.getClass(), o, visited);
    }
}
