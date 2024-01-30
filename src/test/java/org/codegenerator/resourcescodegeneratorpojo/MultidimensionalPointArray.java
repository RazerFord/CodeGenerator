package org.codegenerator.resourcescodegeneratorpojo;

import java.util.Arrays;

public class MultidimensionalPointArray {
    Point[][][] points;

    public Point[][][] getPoints() {
        return points;
    }

    public void setPoints(Point[][][] points) {
        this.points = points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultidimensionalPointArray that = (MultidimensionalPointArray) o;
        return Arrays.deepEquals(points, that.points);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(points);
    }
}
