package org.codegenerator.extractor.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class Leaf implements Node {
    public static final Node NULL_NODE = new Leaf(null, null, null);
    private final Class<?> clazz;
    private final Object value;
    private final Map<Field, Node> fields = Collections.emptyMap();
    private final Map<Object, Node> visited;

    public Leaf(Class<?> clazz, Object value, Map<Object, Node> visited) {
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
    public void extract() {
    }

    @Override
    public NodeType nodeType() {
        return NodeType.LEAF;
    }

    @Override
    public int diff(@NotNull Node that) {
        if (!(that instanceof Leaf)) return -1;
        return value.equals(that.getValue()) ? 0 : 1;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Node remove(Object o) {
        return null;
    }

    @Override
    public void putAll(@NotNull Map<?, ? extends Node> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
    }

    @NotNull
    @Override
    public Set<Object> keySet() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public Collection<Node> values() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public Set<Entry<Object, Node>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Leaf)) return false;
        return value.equals(((Node) o).getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, value);
    }
}
