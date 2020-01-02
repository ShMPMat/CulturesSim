package simulation.culture.group;

import extra.OutputFunc;
import extra.ProbFunc;
import extra.ShnyPair;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.aspect.dependency.*;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.thinking.meaning.Meme;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.Territory;
import simulation.space.Tile;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.Function;
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

    private int maxPopulation = session.defaultGroupMaxPopulation;
    private int fertility = session.defaultGroupFertility;
    private int minPopulationPerTile = session.defaultGroupMinPopulationPerTile;
    private List<Stratum> strata = new ArrayList<>();
    private CulturalCenterOvergroup culturalCenterOvergroup;
    private double spreadability;
    private Territory territory = new Territory();
    ResourcePack resourcePack = new ResourcePack();
    public ResourcePack cherishedResources = new ResourcePack();
    ResourcePack uniqueArtifacts = new ResourcePack();

    private GroupConglomerate(String name, int population, double spreadability, int numberOfSubGroups, Tile root) {
        this.name = name;
        this.population = population;
        this.spreadability = spreadability;
        culturalCenterOvergroup = new CulturalCenterOvergroup(this);
        overgroupClaim(root);

        for (int i = 0; i < numberOfSubGroups; i++) {
            overgroupAddSubgroup(new Group(this, null, name + "_" + i, population / numberOfSubGroups,
                    getCenter()));
        }
    }

    public GroupConglomerate(int numberOfSubgroups, Tile root) {
        this(session.getVacantGroupName(), 100 + ProbFunc.randomInt(100),
                session.defaultGroupSpreadability, numberOfSubgroups, root);
    }

    public Set<Aspect> overgroupGetAspects() {
        return subgroups.stream().map(Group::getAspects).reduce(new HashSet<>(), (x, y) -> {
            x.addAll(y);
            return x;
        });
    }

    public List<CultureAspect> overgroupGetCultureAspects() {//TODO reduce
        return subgroups.get(0).getCulturalCenter().getCultureAspects();
    }

    public List<Meme> overgroupGetMemes() {//TODO reduce
        return subgroups.get(0).getCulturalCenter().getMemePool().getMemes();
    }

    public List<Tile> getTiles() {
        return territory.getTiles();
    }

    int getFertility() {
        return fertility;
    }

    public Territory getTerritory() {
        return territory;
    }

    public List<Event> overgroupGetEvents() {
        return culturalCenterOvergroup.getEvents();
    }

    public List<Stratum> getStrata() {
        return strata;
    }

    public Stratum getStratumByAspect(Aspect aspect) {
        return strata.stream().filter(stratum -> stratum.containsAspect(aspect)).findFirst().orElse(null);
    }

    int getMaxPopulation() {
        return getTerritory().size() * maxPopulation;
    }

    public int getFreePopulation() {
        return population - strata.stream().reduce(0, (x, y) -> x + y.getAmount(), Integer::sum);
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

    public void overgroupUpdate() {
        if (state == State.Dead) {
            return;
        }
        int size = subgroups.size();
        subgroups.forEach(Group::updateRequests);
        subgroups.forEach(Group::executeRequests);
        subgroups.forEach(Group::strataUpdate);
        for (int i = 0; i < size; i++) {
            subgroups.get(i).populationUpdate();
        }
        overgroupUpdatePopulation();
        if (state == State.Dead) {
            return;
        }
        subgroups.forEach(Group::expand);
        subgroups.forEach(group -> group.getCulturalCenter().update());
        for (int i = 0; i < subgroups.size(); i++) {
            Group subgroup = subgroups.get(i);
            if (subgroup.diverge()) {
                i--;
            }
        }
    }

    private void overgroupUpdatePopulation() {
        overgroupComputePopulation();
        if (population == 0) {
            overgroupDie();
        }
    }

    private void overgroupComputePopulation() {
        population = 0;
        for (Group subgroup : subgroups) {
            population += subgroup.population;
        }
    }

    void overgroupClaim(Tile tile) {
        territory.add(tile);
    }

    void overgroupRemove(Tile tile) {
        tile.group = null;
        territory.removeTile(tile);
    }

    void overgroupAddSubgroup(Group subgroup) {
        subgroups.add(subgroup);
        overgroupComputePopulation();
    }

    int overgroupGetDistanceToClosestSubgroup(Tile tile) {
        int d = Integer.MAX_VALUE;
        for (Group subgroup: subgroups) {
            d = min(d, tile.getClosestDistance(Collections.singleton(subgroup.getCenter())));
        }
        return d;
    }

    void overgroupRemoveSubgroup(Group subgroup) {
        population -= subgroup.population;
        if (!subgroups.remove(subgroup)) {
            System.err.println("Trying to remove non-child subgroup " + subgroup.name + " from Group " + name);
        }
    }

    public void overgroupFinishUpdate() {
        subgroups.forEach(Group::finishUpdate);
    }

    private void decreasePopulation(double amount) {
        decreasePopulation((int) amount);
    }

    private void decreasePopulation(int amount) {
        if (getFreePopulation() < 0) {
            int i = 0;//TODO
        }
        amount = min(population, amount);
        int delta = amount - getFreePopulation();
        if (delta > 0) {
            for (Stratum stratum: strata) {
                int part = min(amount * (stratum.getAmount() / population) + 1, stratum.getAmount());
                stratum.freeAmount(part);
            }
        }
        population -= amount;
        if (getFreePopulation() < 0) {
            int i = 0;
        }
    }

    private void removeTile(Tile tile) {
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
