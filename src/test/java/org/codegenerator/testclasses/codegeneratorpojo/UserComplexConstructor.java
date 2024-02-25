package org.codegenerator.testclasses.codegeneratorpojo;

import java.util.Objects;

public class UserComplexConstructor {
    private String name = "";
    private int age;
    private double weight;

    public UserComplexConstructor(String name) {
        this.name = name;
    }

    public UserComplexConstructor(int age, double weight) {
        this.age = age;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserComplexConstructor)) return false;
        UserComplexConstructor user = (UserComplexConstructor) o;
        return user.name.equals(name) && user.age == age && user.weight == weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, weight);
    }
}
