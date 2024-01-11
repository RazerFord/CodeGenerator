package org.codegenerator.resourcescodegeneratorpojo;

import java.util.Arrays;

public class ClassWithArray {
    private int[] arrayOfInt;

    public void setArrayOfInt(int[] arrayOfInt) {
        this.arrayOfInt = arrayOfInt;
    }

    public int[] getArrayOfInt() {
        return arrayOfInt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassWithArray)) return false;
        ClassWithArray classWithArray = (ClassWithArray) o;
        return Arrays.equals(classWithArray.arrayOfInt, arrayOfInt);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arrayOfInt);
    }
}
