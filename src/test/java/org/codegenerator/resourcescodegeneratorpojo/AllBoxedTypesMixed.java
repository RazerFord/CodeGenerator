package org.codegenerator.resourcescodegeneratorpojo;

import java.util.Objects;

public class AllBoxedTypesMixed {
    private Byte aByte;
    private Short aShort;
    private Integer aInt;
    private Long aLong;
    private Float aFloat;
    private Double aDouble;
    private Character aChar;
    private Boolean aBoolean;

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
    public String toString() {
        return "AllBoxedTypesMixed{" +
                "aByte=" + aByte +
                ", aShort=" + aShort +
                ", aInt=" + aInt +
                ", aLong=" + aLong +
                ", aFloat=" + aFloat +
                ", aDouble=" + aDouble +
                ", aChar=" + aChar +
                ", aBoolean=" + aBoolean +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AllBoxedTypesMixed)) return false;
        AllBoxedTypesMixed other = (AllBoxedTypesMixed) o;
        return Objects.equals(other.aByte, aByte) &&
                Objects.equals(other.aShort, aShort) &&
                Objects.equals(other.aInt, aInt) &&
                Objects.equals(other.aLong, aLong) &&
                Objects.equals(other.aFloat, aFloat) &&
                Objects.equals(other.aDouble, aDouble) &&
                Objects.equals(other.aChar, aChar) &&
                Objects.equals(other.aBoolean, aBoolean);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aByte, aShort, aInt, aLong, aFloat, aDouble, aChar, aBoolean);
    }
}
