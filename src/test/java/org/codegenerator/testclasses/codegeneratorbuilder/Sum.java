package org.codegenerator.testclasses.codegeneratorbuilder;

import java.util.Objects;

public class Sum {
    private final int sum;

    private Sum(int sum) {
        this.sum = sum;
    }

    public int getSum() {
        return sum;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Sum sum1 = (Sum) object;
        return sum == sum1.sum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sum);
    }

    public static class Builder {
        private int a;
        private int b;
        private int sum;

        public Builder setA(int a) {
            this.a = a * 2;
            return this;
        }

        public Builder setB(int b) {
            this.b = b * 3;
            return this;
        }

        public Builder calculate() {
            sum = a + b;
            a = b = 0;
            return this;
        }

        public Sum build() {
            return new Sum(sum);
        }
    }
}