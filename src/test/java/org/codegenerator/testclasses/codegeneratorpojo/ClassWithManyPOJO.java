package org.codegenerator.testclasses.codegeneratorpojo;

import java.util.Objects;

public class ClassWithManyPOJO {
    private ClassWithOtherPOJO classWithOtherPOJO;
    private User user;
    private AllPrimitiveTypes allPrimitiveTypes;

    public void setClassWithOtherPOJO(ClassWithOtherPOJO classWithOtherPOJO) {
        this.classWithOtherPOJO = classWithOtherPOJO;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAllPrimitiveTypes(AllPrimitiveTypes allPrimitiveTypes) {
        this.allPrimitiveTypes = allPrimitiveTypes;
    }

    public ClassWithOtherPOJO getClassWithOtherPOJO() {
        return classWithOtherPOJO;
    }

    public User getUser() {
        return user;
    }

    public AllPrimitiveTypes getAllPrimitiveTypes() {
        return allPrimitiveTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassWithManyPOJO)) return false;
        ClassWithManyPOJO classWithManyPOJO = (ClassWithManyPOJO) o;
        return Objects.equals(classWithManyPOJO.classWithOtherPOJO, classWithOtherPOJO) &&
                Objects.equals(classWithManyPOJO.user, user) &&
                Objects.equals(classWithManyPOJO.allPrimitiveTypes, allPrimitiveTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classWithOtherPOJO, user, allPrimitiveTypes);
    }
}
