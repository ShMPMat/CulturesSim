package simulation.culture.group;

import extra.OutputFunc;
import extra.ProbabilityFuncs;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.aspect.dependency.*;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.Territory;
import simulation.space.Tile;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static simulation.Controller.session;

/**
 * Entity with Aspects, which develops through time.
 */
public class GroupConglomerate {
    /**
     * Subgroups on which this group is divided.
     */
    public List<Group> subgroups = new ArrayList<>();
    /**
     * Whether group live or dead
     */
    public State state = State.Live;
    /**
     * Abstract name of the group;
     */
    public String name;
    /**
     * Population of the group. For group with groups must be equal
     * to sum of subgroup populations.
     */
    public int population;
    /**
     * CulturalCenter of this GroupConglomerate
     */
    private CulturalCenterConglomerate culturalCenterOvergroup;
    /**
     * Overall Territory which is under the child Groups
     */
    private Territory territory = new Territory();

    private GroupConglomerate(String name, int population, int numberOfSubGroups, Tile root) {
        this.name = name;
        this.population = population;
        culturalCenterOvergroup = new CulturalCenterConglomerate(this);
        claimTile(root);

        for (int i = 0; i < numberOfSubGroups; i++) {
            addGroup(new Group(this, null, name + "_" + i, population / numberOfSubGroups,
                    getCenter()));
        }
    }

    public GroupConglomerate(int numberOfSubgroups, Tile root) {
        this(session.getVacantGroupName(), 100 + ProbabilityFuncs.randomInt(100), numberOfSubgroups, root);
    }

    /**
     * @return All aspects which exist in any of Groups.
     * Each of them belongs to one of the Groups, it is not guarantied to which one.
     */
    public Set<Aspect> getAspects() {
        return subgroups.stream().map(Group::getAspects).reduce(new HashSet<>(), (x, y) -> {
            x.addAll(y);
            return x;
        });
    }

    /**
     * @return all CultureAspects of child Groups.
     */
    public List<CultureAspect> getCultureAspects() {
        return subgroups.stream().flatMap(group -> group.getCulturalCenter().getCultureAspects().stream())
                .collect(Collectors.toList());
    }

    /**
     * @return all Memes of child Groups.
     */
    public List<Meme> getMemes() {
        return subgroups.stream().map(group -> group.getCulturalCenter().getMemePool().getMemes())
                .reduce(new ArrayList<>(), (x, y) -> {
                    for (Meme meme: y) {
                        int i = x.indexOf(meme);
                        if (i == -1) {
                            x.add(meme.copy());
                        } else {
                            x.get(i).increaseImportance(meme.getImportance());
                        }
                    }
                    return x;
                });
    }

    public List<Tile> getTiles() {
        return territory.getTiles();
    }

    public Territory getTerritory() {
        return territory;
    }

    public List<Event> overgroupGetEvents() {
        return culturalCenterOvergroup.getEvents();
    }

    public Territory getOverallTerritory() {
        return territory;
    }


    private void overgroupDie() {
        state = State.Dead;
        population = 0;
    }

    private void addDependenciesInMap(Map<AspectTag, Set<Dependency>> dep, Collection<Dependency> dependencies,
                                      AspectTag requirement) {
        if (dependencies.isEmpty()) {
            return;
        }
        if (!dep.containsKey(requirement)) {
            dep.put(requirement, new HashSet<>());
        }
        dep.get(requirement).addAll(dependencies);
    }

    public void addEvent(Event event) {
        culturalCenterOvergroup.getEvents().add(event);
    }

    public Tile getCenter() {
        return territory.getCenter();
    }

    private boolean isTileReachable(Tile tile) {
        return getCenter().getDistance(tile) < 4;
    }

    public void update() {//TODO simplify
        if (state == State.Dead) {
            return;
        }
        subgroups.removeIf(group -> group.state == Group.State.Dead);
        int size = subgroups.size();
        subgroups.forEach(Group::updateRequests);
        subgroups.forEach(Group::executeRequests);
        subgroups.forEach(Group::strataUpdate);
        for (int i = 0; i < size; i++) {
            subgroups.get(i).populationUpdate();
        }
        updatePopulation();
        if (state == State.Dead) {
            return;
        }
        subgroups.forEach(group -> group.getTerritoryCenter().update());
        subgroups.forEach(group -> group.getCulturalCenter().update());
        for (int i = 0; i < subgroups.size(); i++) {
            Group subgroup = subgroups.get(i);
            if (subgroup.diverge()) {
                i--;
            }
        }
        subgroups.forEach(Group::intergroupUpdate);
    }

    private void updatePopulation() {
        computePopulation();
        if (population == 0) {
            overgroupDie();
        }
    }

    private void computePopulation() {
        population = 0;
        for (Group subgroup : subgroups) {
            population += subgroup.population;
        }
    }

    void claimTile(Tile tile) {
        territory.add(tile);
    }

    void addGroup(Group group) {
        subgroups.add(group);
        computePopulation();
        group.getTerritory().getTiles().forEach(this::claimTile);
        group.setParentGroup(this);
    }

    int getClosestInnerGroupDistance(Tile tile) {
        int d = Integer.MAX_VALUE;
        for (Group subgroup: subgroups) {
            d = min(d, tile.getClosestDistance(Collections.singleton(subgroup.getCenter())));
        }
        return d;
    }

    void removeGroup(Group group) {
        population -= group.population;
        if (!subgroups.remove(group)) {
            throw new RuntimeException("Trying to remove non-child subgroup " + group.name + " from Group " + name);
        }
        group.getTerritory().getTiles().forEach(this::removeTile);
    }

    public void finishUpdate() {
        subgroups.forEach(Group::finishUpdate);
    }

    void removeTile(Tile tile) {
        if (tile == null) {
            return;
        }
        territory.removeTile(tile);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupConglomerate group = (GroupConglomerate) o;
        return name.equals(group.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Group subgroup : subgroups) {
            stringBuilder = OutputFunc.addToRight(stringBuilder.toString(), subgroup.toString(), false);
        }
        return stringBuilder.toString();
    }

    public enum State {
        Live,
        Dead
    }
}
