package org.codegenerator.testclasses.codegeneratorbuilder;

import java.util.Objects;

public class UserPojo {
    private String name;
    private int age;
    private long created;

    public UserPojo(String name, int age, long created) {
        this.name = name;
        this.age = age;
        this.created = created;
    }

    public String getName() {
        return name;
    }

    public UserPojo setName(String name) {
        this.name = name;
        return this;
    }

    public int getAge() {
        return age;
    }

    public UserPojo setAge(int age) {
        this.age = age;
        return this;
    }

    public long getCreated() {
        return created;
    }

    public UserPojo setCreated(long created) {
        this.created = created;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPojo user = (UserPojo) o;
        return age == user.age && created == user.created && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, created);
    }
}