package org.codegenerator.resourcesgeneric;

import org.codegenerator.resourcesgeneric.persons.Person;

import java.util.Objects;

public class ThreeFieldsWithSpecificBounds<T1 extends Person, T2 extends T1, T3 extends T2> {
    private T1 first;
    private T2 second;
    private T3 third;

    public T1 getFirst() {
        return first;
    }

    public void setFirst(T1 first) {
        this.first = first;
    }

    public T2 getSecond() {
        return second;
    }

    public void setSecond(T2 second) {
        this.second = second;
    }

    public T3 getThird() {
        return third;
    }

    public void setThird(T3 third) {
        this.third = third;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ThreeFieldsWithSpecificBounds<?, ?, ?> that = (ThreeFieldsWithSpecificBounds<?, ?, ?>) object;
        return Objects.equals(first, that.first) && Objects.equals(second, that.second) && Objects.equals(third, that.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }

    @Override
    public String toString() {
        return "ThreeFieldsWithSpecificBounds{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }
}
