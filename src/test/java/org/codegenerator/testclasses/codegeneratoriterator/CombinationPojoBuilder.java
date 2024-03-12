package org.codegenerator.testclasses.codegeneratoriterator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CombinationPojoBuilder {
    private byte aByte;
    private short aShort;
    private int aInt;
    private long count;
    private float aFloat;
    private double aDouble;
    private char aChar;
    private boolean aBoolean;

    public byte getaByte() {
        return aByte;
    }

    public void setaByte(byte aByte) {
        this.aByte = aByte;
    }

    public short getaShort() {
        return aShort;
    }

    public void setaShort(short aShort) {
        this.aShort = aShort;
    }

    public int getaInt() {
        return aInt;
    }

    public void setaInt(int aInt) {
        this.aInt = aInt;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public float getaFloat() {
        return aFloat;
    }

    public void setaFloat(float aFloat) {
        this.aFloat = aFloat;
    }

    public double getaDouble() {
        return aDouble;
    }

    public void setaDouble(double aDouble) {
        this.aDouble = aDouble;
    }

    public char getaChar() {
        return aChar;
    }

    public void setaChar(char aChar) {
        this.aChar = aChar;
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private byte aByte;
        private short aShort;
        private int aInt;
        private long count;
        private float aFloat;
        private double aDouble;
        private char aChar;
        private boolean aBoolean;

        public byte getaByte() {
            return aByte;
        }

        public Builder setaByte(byte aByte) {
            handleSetter();
            this.aByte = aByte;
            return this;
        }

        public short getaShort() {
            return aShort;
        }

        public Builder setaShort(short aShort) {
            handleSetter();
            this.aShort = aShort;
            return this;
        }

        public int getaInt() {
            return aInt;
        }

        public Builder setaInt(int aInt) {
            handleSetter();
            this.aInt = aInt;
            return this;
        }

        public float getaFloat() {
            return aFloat;
        }

        public Builder setaFloat(float aFloat) {
            handleSetter();
            this.aFloat = aFloat;
            return this;
        }

        public double getaDouble() {
            return aDouble;
        }

        public Builder setaDouble(double aDouble) {
            handleSetter();
            this.aDouble = aDouble;
            return this;
        }

        public char getaChar() {
            return aChar;
        }

        public Builder setaChar(char aChar) {
            handleSetter();
            this.aChar = aChar;
            return this;
        }

        public boolean isaBoolean() {
            return aBoolean;
        }

        public Builder setaBoolean(boolean aBoolean) {
            handleSetter();
            this.aBoolean = aBoolean;
            return this;
        }

        private void handleSetter() {
            count++;
        }

        public CombinationPojoBuilder build() {
            CombinationPojoBuilder o = new CombinationPojoBuilder();
            o.setaBoolean(aBoolean);
            o.setaByte(aByte);
            o.setaChar(aChar);
            o.setaDouble(aDouble);
            o.setaFloat(aFloat);
            o.setaInt(aInt);
            o.setaShort(aShort);
            o.setCount(count);
            return o;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CombinationPojoBuilder that = (CombinationPojoBuilder) o;
        return aByte == that.aByte && aShort == that.aShort && aInt == that.aInt && count == that.count && Float.compare(aFloat, that.aFloat) == 0 && Double.compare(aDouble, that.aDouble) == 0 && aChar == that.aChar && aBoolean == that.aBoolean;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aByte, aShort, aInt, count, aFloat, aDouble, aChar, aBoolean);
    }

    @Override
    public String toString() {
        return "CombinationPojoBuilder{" +
                "aByte=" + aByte +
                ", aShort=" + aShort +
                ", aInt=" + aInt +
                ", count=" + count +
                ", aFloat=" + aFloat +
                ", aDouble=" + aDouble +
                ", aChar=" + aChar +
                ", aBoolean=" + aBoolean +
                '}';
    }
}
