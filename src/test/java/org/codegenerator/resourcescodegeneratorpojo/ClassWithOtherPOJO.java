package org.codegenerator.resourcescodegeneratorpojo;

import java.util.Objects;

public class ClassWithOtherPOJO {
    private Point point;

    public void setPoint(Point point) {
        this.point = point;
    }

    public Point getPoint() {
        return point;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassWithOtherPOJO)) return false;
        ClassWithOtherPOJO classWithOtherPOJO = (ClassWithOtherPOJO) o;
        return Objects.equals(classWithOtherPOJO.point, point);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(point);
    }
}
