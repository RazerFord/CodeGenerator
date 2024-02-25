package org.codegenerator.testclasses.codegeneratorpojo;

import java.util.Arrays;

public class Points {
    private Point[] points;

    public Point[] getPoints() {
        return points;
    }

    public Points setPoints(Point[] points) {
        this.points = points;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Points points1 = (Points) o;
        return Arrays.equals(points, points1.points);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(points);
    }
}
