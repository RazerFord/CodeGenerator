package org.codegenerator.testclasses.generic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PointWithBuilder<X, Y, Z> {
    private final X x;
    private final Y y;
    private final Z z;

    public PointWithBuilder(X x, Y y, Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }

    public Z getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointWithBuilder<?, ?, ?> that = (PointWithBuilder<?, ?, ?>) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y) && Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Contract(value = " -> new", pure = true)
    public static <X, Y, Z> @NotNull Builder<X, Y, Z> builder() {
        return new Builder<>();
    }

    public static class Builder<X, Y, Z> {
        private X x;
        private Y y;
        private Z z;

        private Builder() {
        }

        public X getX() {
            return x;
        }

        public Builder<X, Y, Z> setX(X x) {
            this.x = x;
            return this;
        }

        public Y getY() {
            return y;
        }

        public Builder<X, Y, Z> setY(Y y) {
            this.y = y;
            return this;
        }

        public Z getZ() {
            return z;
        }

        public Builder<X, Y, Z> setZ(Z z) {
            this.z = z;
            return this;
        }

        public PointWithBuilder<X, Y, Z> build() {
            return new PointWithBuilder<>(x, y, z);
        }
    }
}
