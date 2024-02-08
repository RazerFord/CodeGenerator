package org.codegenerator.extractor.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Leaf implements Node {
    public static final Node NULL_NODE = new Leaf(null, null, null);
    private static final int POWER = 1;
    private final Class<?> clazz;
    private final Object value;
    private final Map<Object, Node> fields = Collections.emptyMap();

    Leaf(Class<?> clazz, Object value, Map<Object, Node> ignoredVisited) {
        this.clazz = clazz;
        this.value = value;
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
        // this code block must be empty
    }

    @Override
    public NodeType nodeType() {
        return NodeType.LEAF;
    }

    @Override
    public int power() {
        return POWER;
    }

    @Override
    public int diff(@NotNull Node that) {
        if (!(that instanceof Leaf)) return Integer.MAX_VALUE;
        return Objects.equals(value, that.getValue()) ? 0 : POWER;
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
        // this code block must be empty
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
        if (!(o instanceof Leaf)) return false;
        return Objects.equals(value, ((Node) o).getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, value);
    }
}
