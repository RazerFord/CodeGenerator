package org.codegenerator.testclasses.codegeneratorbuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UserWithDefect {
    private String name;
    private int age;
    private long created;

    private UserWithDefect(String name, int age, long created) {
        this.name = name;
        this.age = age;
        this.created = created;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private String name;
        private int age;
        private long created;

        private UserBuilder() {
        }

        public UserBuilder created(long created) {
            this.created = created;
            return this;
        }

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public void age(int age) {
            this.age = age;
        }

        public UserWithDefect build() {
            return new UserWithDefect(name, age, created);
        }

        @Override
        public String toString() {
            return "UserBuilder(created = " + this.created + ", name = " + this.name + ", age = " + this.age + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserWithDefect user = (UserWithDefect) o;
        return age == user.age && created == user.created && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, created);
    }
}