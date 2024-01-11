package org.codegenerator.resourcescodegeneratorpojo;

import java.util.Objects;

public class AllPrimitiveTypesMixed {
    private byte aByte;
    private short aShort;
    private int aInt;
    private long aLong;
    private float aFloat;
    private double aDouble;
    private char aChar;
    private boolean aBoolean;

    public byte getByte() {
        return aByte;
    }

    public short getShort() {
        return aShort;
    }

    public int getInt() {
        return aInt;
    }

    public long getLong() {
        return aLong;
    }

    public float getFloat() {
        return aFloat;
    }

    public double getDouble() {
        return aDouble;
    }

    public char getChar() {
        return aChar;
    }

    public boolean getBoolean() {
        return aBoolean;
    }

    public void setByte(byte aByte) {
        this.aByte = aByte;
    }

    public void setShort(short aShort) {
        this.aShort = aShort;
    }

    public void setInt(int aInt) {
        this.aInt = aInt;
    }

    public void setLong(long aLong) {
        this.aLong = aLong;
    }

    public void setFloat(float aFloat) {
        this.aFloat = aFloat;
    }

    public void setDouble(double aDouble) {
        this.aDouble = aDouble;
    }

    public void setChar(char aChar) {
        this.aChar = aChar;
    }

    public void setBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public void setPart(byte aByte, short aShort, int aInt) {
        setByte(aByte);
        setShort(aShort);
        setInt(aInt);
    }

    public void setPart(char aChar, boolean aBoolean) {
        setChar(aChar);
        setBoolean(aBoolean);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AllPrimitiveTypesMixed)) return false;
        AllPrimitiveTypesMixed other = (AllPrimitiveTypesMixed) o;
        return other.aByte == aByte &&
                other.aShort == aShort &&
                other.aInt == aInt &&
                other.aLong == aLong &&
                other.aFloat == aFloat &&
                other.aDouble == aDouble &&
                other.aChar == aChar &&
                other.aBoolean == aBoolean;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aByte, aShort, aInt, aLong, aFloat, aDouble, aChar, aBoolean);
    }
}
