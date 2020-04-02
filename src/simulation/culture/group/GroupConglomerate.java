package simulation.culture.group;

import extra.OutputFunc;
import simulation.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.Territory;
import simulation.space.resource.MutableResourcePack;
import simulation.space.tile.Tile;
import simulation.space.tile.TileDistanceKt;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static simulation.Controller.session;

public class GroupConglomerate {
    public List<Group> subgroups = new ArrayList<>();
    public State state = State.Live;
    public String name;
    /**
     * Equal to the sum of subgroup populations.
     */
    public int population;
    private List<Event> events = new ArrayList<>();
    /**
     * Overall Territory which is under the child Groups
     */
    private Territory territory = new Territory();
    private int namesScore = 0;

    private GroupConglomerate(String name, int population, int numberOfSubGroups, Tile root) {
        this.name = name;
        this.population = population;
        claimTile(root);

        for (int i = 0; i < numberOfSubGroups; i++) {
            addGroup(new Group(
                    new ResourceCenter(new MutableResourcePack(), getCenter()),
                    this,
                    name + "_" + i,
                    new PopulationCenter(
                            population / numberOfSubGroups,
                            session.defaultGroupMaxPopulation,
                            session.defaultGroupMinPopulationPerTile
                    ),
                    new RelationCenter((t, g) -> (GroupsKt.getGroupsDifference(t, g) - 1) / 2),
                    getCenter(),
                    new ArrayList<>(),
                    new GroupMemes(),
                    session.defaultGroupSpreadability
            ));
        }
    }

    public GroupConglomerate(int numberOfSubgroups, Tile root) {
        this(session.getVacantGroupName(), 100 + session.random.nextInt(100), numberOfSubgroups, root);
    }

    /**
     * @return All aspects which exist in any of Groups.
     * Each of them belongs to one of the Groups, it is not guarantied to which one.
     */
    public Set<Aspect> getAspects() {
        return subgroups.stream()
                .flatMap(g -> g.getCultureCenter().getAspectCenter().getAspectPool().getAll().stream())
                .collect(Collectors.toSet());
    }

    /**
     * @return all CultureAspects of child Groups.
     */
    public Set<CultureAspect> getCultureAspects() {
        return subgroups.stream()
                .flatMap(group -> group.getCultureCenter().getCultureAspectCenter().getAspectPool().getAll().stream())
                .collect(Collectors.toSet());
    }

    /**
     * @return all Memes of child Groups.
     */
    public List<Meme> getMemes() {
        return subgroups.stream().map(group -> group.getCultureCenter().getMemePool().getMemes())
                .reduce(new ArrayList<>(), (x, y) -> {
                    for (Meme meme : y) {
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

    public Territory getTerritory() {
        return territory;
    }

    public List<Event> getEvents() {
        return events;
    }

    private void overgroupDie() {
        state = State.Dead;
        population = 0;
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public Tile getCenter() {
        return territory.getCenter();
    }

    public void update() {//TODO simplify
        if (state == State.Dead) {
            return;
        }
        subgroups.removeIf(group -> group.state == Group.State.Dead);
        subgroups.forEach(Group::update);
        Collection<Group> newGroups = subgroups.stream()
                .map(Group::populationUpdate)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        newGroups.forEach(this::addGroup);
        updatePopulation();
        if (state == State.Dead) {
            return;
        }
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
            population += subgroup.getPopulationCenter().getPopulation();
        }
    }

    void claimTile(Tile tile) {
        territory.add(tile);
    }

    void addGroup(Group group) {
        subgroups.add(group);
        computePopulation();
        group.getTerritoryCenter().getTerritory().getTiles().forEach(this::claimTile);
        group.setParentGroup(this);
    }

    int getClosestInnerGroupDistance(Tile tile) {
        int d = Integer.MAX_VALUE;
        for (Group subgroup : subgroups) {
            d = min(
                    d,
                    TileDistanceKt.getClosest(
                            tile,
                            Collections.singleton(subgroup.getTerritoryCenter().getTerritory().getCenter())
                    ).getSecond()
            );
        }
        return d;
    }

    void removeGroup(Group group) {
        population -= group.getPopulationCenter().getPopulation();
        if (!subgroups.remove(group)) {
            throw new RuntimeException("Trying to remove non-child subgroup " + group.name + " from Group " + name);
        }
        group.getTerritoryCenter().getTerritory().getTiles().forEach(this::removeTile);
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

    public String getNewName() {
        namesScore++;
        return name + "_" + namesScore;
    }

    public enum State {
        Live,
        Dead
    }
}
