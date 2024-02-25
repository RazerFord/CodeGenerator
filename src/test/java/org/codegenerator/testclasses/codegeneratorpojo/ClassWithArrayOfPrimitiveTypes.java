package org.codegenerator.testclasses.codegeneratorpojo;

import java.util.Arrays;

public class ClassWithArrayOfPrimitiveTypes {
    private int[] arrayOfInt;

    public void setArrayOfInt(int[] arrayOfInt) {
        this.arrayOfInt = arrayOfInt;
    }

    public int[] getArrayOfInt() {
        return arrayOfInt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassWithArrayOfPrimitiveTypes)) return false;
        ClassWithArrayOfPrimitiveTypes classWithArrayOfPrimitiveTypes = (ClassWithArrayOfPrimitiveTypes) o;
        return Arrays.equals(classWithArrayOfPrimitiveTypes.arrayOfInt, arrayOfInt);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arrayOfInt);
    }
}
