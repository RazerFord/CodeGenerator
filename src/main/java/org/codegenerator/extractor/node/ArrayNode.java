package org.codegenerator.extractor.node;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;

import static org.codegenerator.CommonUtils.throwUnless;

public class ArrayNode implements DiffNode {
    private boolean visited = false;
    private final Class<?> clazz;
    private final Object value;
    private final Map<Object, Node> fields;
    private final Supplier<Integer> power;

    ArrayNode(@NotNull Class<?> clazz, Object value, @NotNull Map<Object, Node> visited) {
        Supplier<? extends RuntimeException> supplier = () -> new IllegalArgumentException("ArrayNode");
        throwUnless(clazz.isArray(), supplier);

        this.clazz = clazz;
        this.value = value;

        visited.put(value, this);
        fields = extract(visited);
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
    public NodeType nodeType() {
        return NodeType.ARRAY;
    }

    @Override
    public int power() {
        if (visited) return 0;
        visited = true;
        int p = power.get();
        visited = false;
        return p;
    }

    @Override
    public int diff(Node that) {
        return diff(that, Collections.newSetFromMap(new IdentityHashMap<>()));
//        if (visited) return 0;
//        visited = true;
//        if (!(that instanceof ArrayNode)) return power();
//        int diff = 0;
//        for (Map.Entry<Object, Node> entry : fields.entrySet()) {
//            DiffNode n1 = (DiffNode) entry.getValue();
//            DiffNode n2 = (DiffNode) that.get(entry.getKey());
//            int curDiff = NodeUtils.diff(n1, n2);
//            // diff + curDiff >= MAX => curDiff >= MAX - diff
//            if (curDiff >= Integer.MAX_VALUE - diff) return Integer.MAX_VALUE;
//            diff += curDiff;
//        }
//        visited = false;
//        return diff;
    }

    @Override
    public void accept(@NotNull NodeVisitor visitor) {
        if (visited) return;
        visited = true;
        visitor.visit(this);
        visited = false;
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

    @Override
    public @NotNull Set<Entry<Object, Node>> entrySet() {
        return fields.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArrayNode)) return false;
        if (visited) return true;
        visited = true;
        ArrayNode arrayNode = (ArrayNode) o;
        boolean result = Objects.equals(value, arrayNode.value) && arrayNode.fields.equals(fields);
        visited = false;
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, value, fields.keySet());
    }

    private @NotNull Map<Object, Node> extract(@NotNull Map<Object, Node> visited) {
        Map<Object, Node> map = new HashMap<>();
        int length = Array.getLength(value);
        for (int i = 0; i < length; i++) {
            Node node = visited.get(Array.get(value, i));
            if (node != null) {
                map.put(i, node);
            } else {
                map.put(i, NodeUtils.createNode(Array.get(value, i), visited));
            }
        }
        return map;
    }

    @Override
    public int diff(Node that, @NotNull Set<Node> visitedNode) {
        if (visitedNode.contains(this) && visitedNode.contains(that)) return 0;
        if (visitedNode.contains(this)) return ((DiffNode)that).power(visitedNode);
        if (visitedNode.contains(that)) return power(visitedNode);
        if (!(that instanceof ArrayNode)) return power();
        visitedNode.add(this);
        visitedNode.add(that);
        visited = true;
        int diff = 0;
        for (Map.Entry<Object, Node> entry : fields.entrySet()) {
            DiffNode n1 = (DiffNode) entry.getValue();
            DiffNode n2 = (DiffNode) that.get(entry.getKey());
            int curDiff = NodeUtils.diff(n1, n2, visitedNode);
            // diff + curDiff >= MAX => curDiff >= MAX - diff
            if (curDiff >= Integer.MAX_VALUE - diff) return Integer.MAX_VALUE;
            diff += curDiff;
        }
        visited = false;
        return diff;
    }

    @Override
    public int power(@NotNull Set<Node> visited) {
        if (visited.contains(this)) return 0;
        return fields
                .values()
                .stream()
                .filter(visited::add)
                .mapToInt(Node::power).sum();
    }
}
