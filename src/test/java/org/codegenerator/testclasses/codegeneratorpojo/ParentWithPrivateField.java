package org.codegenerator.testclasses.codegeneratorpojo;

import java.util.Objects;

public class ParentWithPrivateField {
    private String name;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ParentWithPrivateField that = (ParentWithPrivateField) object;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
