package simulation.culture.group.intergroup;

import simulation.culture.group.Group;

public class Relation {
    public Group owner;
    public Group other;

    double positive = 0;

    private Relation pair;

    public Relation(Group owner, Group other) {
        this.owner = owner;
        this.other = other;
    }

    public double getPositive() {
        return positive;
    }

    public void setPair(Relation pair) {
        this.pair = pair;
    }

    public void setPositive(double positive) {
        this.positive = positive;
        if (this.positive > 1) positive = 1;
        if (this.positive < -1) positive = -1;
    }

    public void increaseRelationsTwoSided(double delta) {
        setPositive(positive + delta);
        pair.setPositive(pair.positive + delta);
    }

    @Override
    public String toString() {
        return String.format("%s is %f", other.name, positive);
    }
}
