package org.codegenerator.resourcescodegeneratorbuilder;

import java.util.Arrays;
import java.util.Objects;

public class UserWithBuilderInAnotherPackage {
    private String name;
    private int age;
    private long created;
    private long[] coins;

    public UserWithBuilderInAnotherPackage(String name, int age, long created, long[] coins) {
        this.name = name;
        this.age = age;
        this.created = created;
        this.coins = coins;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserWithBuilderInAnotherPackage user = (UserWithBuilderInAnotherPackage) o;
        return age == user.age && created == user.created && Objects.equals(name, user.name) && Arrays.equals(coins, user.coins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, created, Arrays.hashCode(coins));
    }
}
