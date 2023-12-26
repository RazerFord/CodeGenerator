package org.codegenerator.extractor.node;

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
                Node node = Node.createNode(field, o, visited);
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
    public NodeType nodeType() {
        return NodeType.INNER;
    }

    @Override
    public int diff(Node that) {
        if (!(that instanceof InnerNode)) return Integer.MAX_VALUE;
        int diff = 0;
        for (Map.Entry<Field, Node> entry : fields.entrySet()) {
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
    public Node put(Object field, Node node) {
        if (field instanceof Field) {
            return fields.put((Field) field, node);
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
                fields.put((Field) e.getKey(), e.getValue());
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
        if (!(o instanceof InnerNode)) return false;
        return entrySet().equals(((Node) o).entrySet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, entrySet());
    }
}
