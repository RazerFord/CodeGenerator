package org.codegenerator.resourcesCodeGeneratorPOJO;

public class Point {
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

    public void setY(int yy) {
        y = yy;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setAll(int xx, int yy, int zz) {
        x = xx;
        y = yy;
        z = zz;
    }
}
