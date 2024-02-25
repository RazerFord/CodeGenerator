package org.codegenerator.testclasses.codegeneratorbuilder.otherpackage;

import org.codegenerator.testclasses.codegeneratorbuilder.UserWithBuilderInAnotherPackage;

public class UserBuilder {
    private String name;
    private int age;
    private long created;
    private long[] coins;

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

    public UserBuilder coins(long[] coins) {
        this.coins = coins;
        return this;
    }

    public UserWithBuilderInAnotherPackage build() {
        return new UserWithBuilderInAnotherPackage(name, age, created, coins);
    }

    @java.lang.Override
    public String toString() {
        return "UserBuilder(created = " + this.created + ", name = " + this.name + ", age = " + this.age + ")";
    }
}
