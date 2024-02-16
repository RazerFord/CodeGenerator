package org.codegenerator.resourcesgeneric;

import java.util.Arrays;

public class FieldArray<T> {
    private T[] array;

    public T[] getArray() {
        return array;
    }

    public void setArray(T[] array) {
        this.array = array;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        FieldArray<?> that = (FieldArray<?>) object;
        return Arrays.equals(array, that.array);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public String toString() {
        return "FieldArray{" +
                "array=" + Arrays.toString(array) +
                '}';
    }
}
