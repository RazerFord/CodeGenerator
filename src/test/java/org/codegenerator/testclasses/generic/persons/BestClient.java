package org.codegenerator.testclasses.generic.persons;

import java.util.Objects;

public class BestClient extends Client {
    private int skill;

    public BestClient(String name, int skill) {
        super(name);
        this.skill = skill;
    }

    public int getSkill() {
        return skill;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        BestClient that = (BestClient) object;
        return skill == that.skill;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), skill);
    }

    @Override
    public String toString() {
        return "BestClient{" +
                "skill=" + skill +
                '}';
    }
}
