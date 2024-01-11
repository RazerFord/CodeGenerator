package org.codegenerator.resourcescodegeneratorpojo;

import java.util.Objects;

public class PointComplex {
    private int x;
    private int y;
    private int z;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setXZ(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public void setYZ(int y, int z) {
        this.y = y;
        this.z = z;
    }

    public void setXYZ(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setY(int yy) {
        y = yy;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PointComplex)) return false;
        PointComplex point = (PointComplex) o;
        return point.x == x && point.y == y && point.z == z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
