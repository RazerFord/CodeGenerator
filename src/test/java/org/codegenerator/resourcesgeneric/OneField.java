package org.codegenerator.resourcesgeneric;

import java.util.Objects;

public class OneField<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        OneField<?> oneField = (OneField<?>) object;
        return Objects.equals(value, oneField.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "OneField{" +
                "value=" + value +
                '}';
    }
}
