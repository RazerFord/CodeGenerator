package org.codegenerator.extractor.node;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

import static org.codegenerator.CommonUtils.throwUnless;

public class Leaf implements Node {
    protected static final Node NULL_NODE = new Leaf(null, null);
    private static final int POWER = 1;
    private final Class<?> clazz;
    private final Object value;
    private final Map<Object, Node> fields = Collections.emptyMap();

    Leaf(Class<?> clazz, Object value) {
        Supplier<? extends RuntimeException> supplier = () -> new IllegalArgumentException("Leaf");
        throwUnless(ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class || value == null, supplier);

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
    public NodeType nodeType() {
        return NodeType.LEAF;
    }

    @Override
    public int power() {
        return POWER;
    }

    @Override
    public int diff(@NotNull Node that) {
        if (!(that instanceof Leaf)) return power();
        return Objects.equals(value, that.getValue()) ? 0 : power();
    }

    @Override
    public void accept(@NotNull NodeVisitor visitor) {
        visitor.visit(this);
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
