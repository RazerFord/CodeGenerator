package org.codegenerator.resourcescodegeneratorpojo;

import java.util.Arrays;

public class MultidimensionalIntArray {
    int[][][] ints;

    public void setInts(int[][][] ints) {
        this.ints = ints;
    }

    public int[][][] getInts() {
        return ints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultidimensionalIntArray that = (MultidimensionalIntArray) o;
        return Arrays.deepEquals(ints, that.ints);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(ints);
    }
}
