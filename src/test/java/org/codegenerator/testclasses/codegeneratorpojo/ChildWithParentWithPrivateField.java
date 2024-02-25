package org.codegenerator.testclasses.codegeneratorpojo;

public class ChildWithParentWithPrivateField extends ParentWithPrivateField {
    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
