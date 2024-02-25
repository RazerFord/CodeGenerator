package org.codegenerator.testclasses.codegeneratorpojo;

import java.util.Objects;

public class AccumulatorHolder {
    private Accumulator a;
    private Accumulator b;
    private Accumulator c;

    public Accumulator getA() {
        return a;
    }

    public void setA(Accumulator a) {
        this.a = a;
    }

    public Accumulator getB() {
        return b;
    }

    public void setB(Accumulator b) {
        this.b = b;
    }

    public void calc() {
        a.calculate();
        b.calculate();
        c = new Accumulator();
        c.setA(a.getSum());
        c.setB(b.getSum());
        c.calculate();
        a = null;
        b = null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AccumulatorHolder that = (AccumulatorHolder) object;
        return Objects.equals(a, that.a) && Objects.equals(b, that.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public String toString() {
        return "AccumulatorHolder{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }
}