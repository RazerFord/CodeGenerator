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
    public Node put(Field field, Node node) {
        return fields.put(field, node);
    }

    @Override
    public Node remove(Object o) {
        return fields.remove(o);
    }

    @Override
    public void putAll(@NotNull Map<? extends Field, ? extends Node> map) {
        fields.putAll(map);
    }

    @Override
    public void clear() {
        fields.clear();
    }

    @NotNull
    @Override
    public Set<Field> keySet() {
        return fields.keySet();
    }

    @NotNull
    @Override
    public Collection<Node> values() {
        return fields.values();
    }

    @NotNull
    @Override
    public Set<Entry<Field, Node>> entrySet() {
        return fields.entrySet();
    }
}
