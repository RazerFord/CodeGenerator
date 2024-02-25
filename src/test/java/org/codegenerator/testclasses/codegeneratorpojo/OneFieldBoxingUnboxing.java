package org.codegenerator.testclasses.codegeneratorpojo;

import java.util.Objects;

public class OneFieldBoxingUnboxing {
    private Integer i;
    private int j;

    public void setI(int i) {
        this.i = i;
    }

    public void setJ(Integer j) {
        this.j = j;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OneFieldBoxingUnboxing)) return false;
        OneFieldBoxingUnboxing that = (OneFieldBoxingUnboxing) o;
        return Objects.equals(that.i, i) && that.j == j;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, j);
    }
}
