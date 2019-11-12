package simulation.culture.group;

import simulation.culture.aspect.Aspect;
import simulation.space.Territory;

import java.util.ArrayList;
import java.util.List;

public class GroupConglomerate {
    String name;
    Group.State state;
    List<Group> groups = new ArrayList<>();
    Territory _territory = new Territory();
    List<Aspect> _aspects = new ArrayList<>();
    int population;

    private Group temporaryCrutch;

    public GroupConglomerate(String name) {
        this.name = name;
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public void removeGroup(Group group) {
        if (!groups.remove(group)) {
            System.err.println("Trying to remove non-child group " + group.name + " from Conglomerate " + name);
        }
    }

    void recomputeTerritory() {
        groups.forEach(group -> _territory.addAll(group.getTerritory()));
    }

    void recomputeAspects() {
        //TODO
    }

    void recomputePopulation() {
        population = groups.stream().reduce(0, (x, y) -> x + y.population, Integer::sum);
    }
}
