package org.codegenerator.resourcescodegeneratorbuilder;

import java.util.Objects;

public class UserBuilderWithConstructor {
    private String name;
    private int age;
    private long created;

    private UserBuilderWithConstructor(String name, int age, long created) {
        this.name = name;
        this.age = age;
        this.created = created;
    }

    public static class UserBuilder {
        private String name;
        private int age;
        private long created;

        public UserBuilder() {
        }

        public UserBuilder created(long created) {
            this.created = created;
            return this;
        }

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder age(int age) {
            this.age = age;
            return this;
        }

        public UserBuilderWithConstructor build() {
            return new UserBuilderWithConstructor(name, age, created);
        }

        @Override
        public String toString() {
            return "User.BuilderExampleBuilder(created = " + this.created + ", name = " + this.name + ", age = " + this.age + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBuilderWithConstructor that = (UserBuilderWithConstructor) o;
        return age == that.age && created == that.created && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, created);
    }
}