package org.codegenerator.resourcesCodeGeneratorPOJO;

import java.util.Objects;

public class User {
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return user.name.equals(name) && user.age == age && user.weight == weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, weight);
    }
}
