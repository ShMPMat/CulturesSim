package simulation.culture.group;

import simulation.culture.aspect.Aspect;
import simulation.space.Territory;

import java.util.*;

public class GroupConglomerate {
    String name;
    Group.State state;
    List<Group> groups = new ArrayList<>();
    Territory _territory = new Territory();
    Set<Aspect> _aspects = new HashSet<>();
    int population;

    public GroupConglomerate(Group group) {
        this.name = name;
        addGroup(group);
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
        _aspects = new HashSet<>();
        for (Group group: groups) {
            for (Aspect aspect: group.getAspects()) {
                if (aspect.getUsefulness() > 0) {
                    continue;
                }
                _aspects.add(aspect);
            }
        }
    }

    void recomputePopulation() {
        population = groups.stream().reduce(0, (x, y) -> x + y.population, Integer::sum);
    }

    @Override
    public String toString() {
        return name;
    }
}
