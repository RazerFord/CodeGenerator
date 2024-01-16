package org.codegenerator.resourcescodegeneratorbuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class User {
    private String name;
    private int age;
    private long created;

    private User(String name, int age, long created) {
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

        public UserBuilder age(int age) {
            this.age = age;
            return this;
        }

        public User build() {
            return new User(name, age, created);
        }

        @java.lang.Override
        public String toString() {
            return "User.BuilderExampleBuilder(created = " + this.created + ", name = " + this.name + ", age = " + this.age + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return age == user.age && created == user.created && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, created);
    }
}