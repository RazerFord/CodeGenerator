package org.codegenerator.resourcescodegeneratorpojo;

import java.util.Objects;

public class Accumulator {
    private int a;
    private int b;
    private int sum;

    public void setA(int a) {
        this.a = a;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void calculate() {
        sum = a + b;
        a = b = 0;
    }

    public int getSum() {
        return sum;
    }

    @Override
    public String toString() {
        return "Sum{" +
                "a=" + a +
                ", b=" + b +
                ", sum=" + sum +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Accumulator that = (Accumulator) object;
        return a == that.a && b == that.b && sum == that.sum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, sum);
    }
}