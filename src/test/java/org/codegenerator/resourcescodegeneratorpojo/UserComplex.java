package org.codegenerator.resourcescodegeneratorpojo;

import java.util.Objects;

public class UserComplex {
    private String name = "";
    private int age;
    private double weight;

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

    public void setNameAge(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void setAgeWeight(int age, double weight) {
        this.age = age;
        this.weight = weight;
    }

    public void setWeightName(double weight, String name) {
        this.weight = weight;
        this.name = name;
    }

    public void setNameAgeWeight(String name, int age, double weight) {
        this.weight = weight;
        this.age = age;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserComplex)) return false;
        UserComplex user = (UserComplex) o;
        return user.name.equals(name) && user.age == age && user.weight == weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, weight);
    }
}
