package org.codegenerator.extractor.node;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class InnerNode implements Node {
    private final Class<?> clazz;
    private final Object value;
    private final Map<Field, Node> fields = new HashMap<>();
    private final Map<Object, Node> visited;

    public InnerNode(Class<?> clazz, Object value, Map<Object, Node> visited) {
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
        Class<?> clz = clazz;
        List<Node> unvisitedNodes = new ArrayList<>();

        while (clz != null) {
            Field[] fields1 = clz.getDeclaredFields();
            for (Field field : fields1) {
                field.setAccessible(true);
                Object o = field.get(value);
                Node node = createNode(field, o);
                Node nextNode = visited.putIfAbsent(o, node);
                if (nextNode != null) {
                    node = nextNode;
                } else {
                    unvisitedNodes.add(node);
                }
                fields.putIfAbsent(field, node);
            }
            clz = clz.getSuperclass();
        }
        for (Node node : unvisitedNodes) {
            node.extract();
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
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

    @Contract("_, _ -> new")
    private @NotNull Node createNode(@NotNull Field field, Object o) {
        if (o == null) {
            return Leaf.NULL_NODE;
        }
        if (field.getType().isPrimitive()) {
            return new Leaf(o.getClass(), o, visited);
        }
        return new InnerNode(o.getClass(), o, visited);
    }
}
