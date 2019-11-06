package simulation.culture.group;

import extra.OutputFunc;
import extra.ProbFunc;
import extra.ShnyPair;
import simulation.World;
import simulation.culture.Event;
import simulation.culture.aspect.*;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.Territory;
import simulation.space.Tile;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Entity with Aspects, which develops through time.
 */
public class Group {
    /**
     * Subgroups on which this group is divided.
     */
    public List<Group> subgroups;
    /**
     * Whether group live or dead
     */
    public State state;
    /**
     * Abstract name of the group;
     */
    public String name;
    /**
     * Population of the group. For group with subgroups must be equal
     * to sum of subgroup populations.
     */
    public int population;

    private int maxPopulation = 1000;
    private int fertility = 10;
    private int minPopulationPerTile = 1;
    private List<Stratum> strata;
    private Group parentGroup;
    private CulturalCenter culturalCenter;
    private double spreadability;
    private Territory territory;
    private Function<Tile, Integer> tileValueMapper = t -> t.getNeighbours(tile1 -> this.equals(tile1.group)).size() -
            3 * t.closestTileWithResources(getResourceRequirements());
    ResourcePack resourcePack;
    ResourcePack cherishedResources, uniqueArtefacts;

    private Group(String name, World world, int population, double spreadability, int numberOfSubGroups, Tile root) {
        this.culturalCenter = new CulturalCenter(this, world);
        getCulturalCenter().setChangedAspects(new HashSet<>());
        this.resourcePack = new ResourcePack();
        this.cherishedResources = new ResourcePack();
        this.uniqueArtefacts = new ResourcePack();
        this.strata = new ArrayList<>();
        this.parentGroup = null;
        this.name = name;
        setAspects(new HashSet<>());
        this.state = State.Live;
        this.population = population;
        this.spreadability = spreadability;
        setEvents(new ArrayList<>());

        territory = new Territory();
        if (root == null) {
            while (true) {
                Tile tile = ProbFunc.randomTile(world.map);
                if (tile.group == null && tile.canSettle(this)) {
                    claimTile(tile);
                    break;
                }
            }
        } else {
            territory.addTile(root);
        }

        this.subgroups = new ArrayList<>();
        for (int i = 0; i < numberOfSubGroups; i++) {
            subgroups.add(new Group(this, name + "_" + i, population / numberOfSubGroups,
                    territory.getTileByNumber(0)));
        }
    }

    public Group(String name, World world, int population, double spreadability) {
        this(name, world, population, spreadability, 1, null);
    }

    private Group(Group group, String name, int population, Tile tile) {
        this(name, group.getCulturalCenter().world, population, 0, 0, tile);
        this.parentGroup = group;
        for (Aspect aspect : group.getAspects()) {
            getCulturalCenter().addAspectNow(aspect, aspect.getDependencies());
            finishUpdate();
        }
//        for (Aspect aspect : group.culturalCenter.getChangedAspects()) { //TODO Why?
//            getCulturalCenter().addAspectNow(aspect, aspect.getDependencies());
//        }
    }

    public Set<Aspect> getAspects() {
        return getCulturalCenter().getAspects();
    }

    public Aspect getAspect(Aspect aspect) {
        return culturalCenter.getAspect(aspect);
    }

    public List<Tile> getTiles() {
        return territory.getTiles();
    }

    /**
     * link to the main World class instance.
     */
    public CulturalCenter getCulturalCenter() {
        return culturalCenter;
    }

    int getFertility() {
        return fertility;
    }

    public Territory getTerritory() {
        return territory;
    }

    public List<Event> getEvents() {
        return getCulturalCenter().getEvents();
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
        return (getParentGroup() == null ? territory : getParentGroup().getOverallTerritory());
    }

    public Group getOverallGroup() {
        return (getParentGroup() == null ? this : getParentGroup().getOverallGroup());
    }

    private Collection<Resource> getResourceRequirements() {
        return getAspects().stream().filter(aspect -> aspect instanceof ConverseWrapper)
                .map(aspect -> ((ConverseWrapper) aspect).resource).distinct().collect(Collectors.toList());
    }

    Group getParentGroup() {
        return parentGroup;
    }

    private int getDistanceToClosestSubgroup(Tile tile) {
        int d = Integer.MAX_VALUE;
        for (Group subgroup: subgroups) {
            d = Math.min(d, tile.getClosestDistance(subgroup.getTerritory().getTiles()));
        }
        return d;
    }

    private void die() {
        state = State.Dead;
        population = 0;
        for (Tile tile : territory.getTiles()) {
            tile.group = null;
        }
        cherishedResources.disbandOnTile(ProbFunc.randomElement(territory.getTiles()));
        uniqueArtefacts.disbandOnTile(ProbFunc.randomElement(territory.getTiles()));
        addEvent(new Event(Event.Type.Death, getCulturalCenter().world.getTurn(), "Group " + name + " died", "group", this));
        for (Group group : culturalCenter.world.map.getAllNearGroups(this)) {
            group.culturalCenter.addMemeCombination(culturalCenter.world.getMemeFromPoolByName("group")
                    .addPredicate(new MemeSubject(name)).addPredicate(culturalCenter.world.getMemeFromPoolByName("die")));
        }
    }

    private void setAspects(Set<Aspect> aspects) {
        getCulturalCenter().setAspects(aspects);
    }

    private void setEvents(List<Event> events) {
        getCulturalCenter().setEvents(events);
    }

    Map<AspectTag, Set<Dependency>> canAddAspect(Aspect aspect) {
        Map<AspectTag, Set<Dependency>> dep = new HashMap<>();
        if (aspect instanceof ConverseWrapper) {
            addForConverseWrapper((ConverseWrapper) aspect, dep);
        }
        for (AspectTag requirement : aspect.getRequirements()) {
            if (requirement.name.equals("phony")) {
                continue;
            }
            addResourceDependencies(requirement, dep);
            addAspectDependencies(requirement, dep);
        }
        return dep;
    }

    private void addForConverseWrapper(ConverseWrapper converseWrapper, Map<AspectTag, Set<Dependency>> dep) {
        if (converseWrapper.resource.hasApplicationForAspect(converseWrapper.aspect)) {
            if (getOverallTerritory().getDifferentResources().contains(converseWrapper.resource)) {
                addDependenciesInMap(dep, Collections.singleton(new Dependency(converseWrapper.getRequirement(), this,
                        new ShnyPair<>(converseWrapper.resource, converseWrapper.aspect))), converseWrapper.getRequirement());
            }
            addDependenciesInMap(dep, getCulturalCenter().getAllProducedResources().stream()
                            .filter(pair -> pair.first.equals(converseWrapper.resource))
                            .map(pair -> new Dependency(converseWrapper.getRequirement(), new ShnyPair<>(converseWrapper, pair.second),
                                    this)).filter(dependency -> !dependency.isCycleDependency(converseWrapper)).collect(Collectors.toList()),
                    converseWrapper.getRequirement());
        }
    }

    private void addResourceDependencies(AspectTag requirement, Map<AspectTag, Set<Dependency>> dep) {
        List<Resource> _r = territory.getResourcesWithAspectTag(requirement);
        if (_r != null) {
            addDependenciesInMap(dep, _r.stream().map(resource -> new Dependency(requirement, this, resource))
                    .collect(Collectors.toList()), requirement);
        }
    }

    private void addAspectDependencies(AspectTag requirement, Map<AspectTag, Set<Dependency>> dep) {
        for (Aspect selfAspect : getAspects()) {
            if (selfAspect.getTags().contains(requirement)) {
                Dependency dependency = new Dependency(requirement, this, selfAspect);
                if (dependency.isCycleDependency(selfAspect)) {
                    continue;
                }
                addDependenciesInMap(dep, Collections.singleton(dependency), requirement);
            }
//            addDependenciesInMap(dep, culturalCenter.getAllProducedResources().stream()
//                    .filter(pair -> pair.first.));
            addDependenciesInMap(dep, territory.getResourcesWhichConverseToTag(selfAspect, requirement).stream() //Make converse Dependency
                            .map(resource -> new Dependency(requirement, this, new ShnyPair<>(resource, selfAspect)))
                            .collect(Collectors.toList()),
                    requirement);
        }
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
        getEvents().add(event);
    }

    public int changeStratumAmountByAspect(Aspect aspect, int amount) {
        Stratum stratum = getStratumByAspect(aspect);
        if (stratum.getAmount() < amount) {
            amount = Math.min(amount, getFreePopulation());
        }
        stratum.setAmount(amount);
        return amount;
    }

    public void update(double rAspectAcquisition, double rAspectLending) {
        if (state == State.Dead) {
            return;
        }
        updateRequests();
        executeRequests();
        populationUpdate();
        if (state == State.Dead) {
            return;
        }
        expand();
        if (!subgroups.isEmpty()) {//TODO remove when ready to split
            getCulturalCenter().update(rAspectAcquisition, rAspectLending);
        }
    }

    private void updateRequests() {
        if (subgroups.isEmpty()) {
            getCulturalCenter().updateRequests();
            return;
        }
        for (Group subgroup : subgroups) {
            subgroup.updateRequests();
        }
    }

    private void executeRequests() {
        if (!subgroups.isEmpty()) {
            for (Group subgroup : subgroups) {
                subgroup.executeRequests();
            }
            return;
        }
        for (Request request : getCulturalCenter().getRequests()) { //TODO do smth about getting A LOT MORE resources than planned due to one to many resource conversion
            List<ShnyPair<Stratum, ResourceEvaluator>> pairs = strata.stream()
                    .map(stratum -> new ShnyPair<>(stratum, request.isAcceptable(stratum)))
                    .filter(pair -> pair.second != null).sorted(Comparator.comparingInt(pair ->
                            request.satisfactionLevel(pair.first)))
                    .collect(Collectors.toList());
            for (ShnyPair<Stratum, ResourceEvaluator> pair : pairs) {
                int amount = request.howMuchOfNeeded(resourcePack);
                if (amount > request.ceiling) {
                    break;
                }
                resourcePack.add(pair.first.use(request.ceiling - amount, pair.second));
            }
            request.end(resourcePack);
        }
    }

    private void populationUpdate() {
        if (state == State.Dead) {
            return;
        }
        if (!subgroups.isEmpty()) {
            population = 0;
            for (Group subgroup : subgroups) {
                population += subgroup.population;
            }
            if (population == 0) {
                die();
                subgroups.forEach(Group::die);
            }
            for (int i = 0; i < subgroups.size(); i++) {
                subgroups.get(i).populationUpdate();
            }
        } else {
            if (getMaxPopulation() == population && parentGroup.subgroups.size() < 10) {
                List<Tile> tiles = getOverallTerritory().getTilesWithPredicate(t -> parentGroup.getDistanceToClosestSubgroup(t) > 2);
                if (tiles.isEmpty()) {
                    return;
                }
                population = population / 2;
                Tile tile = ProbFunc.randomElement(tiles);
                Group group = new Group(parentGroup, parentGroup.name + "_" + parentGroup.subgroups.size(),
                        population, tile);
                for (Stratum stratum: strata) {
                    group.strata.get(group.strata.indexOf(stratum)).setAmount(stratum.getAmount() / 2);
                    stratum.setAmount(stratum.getAmount() - (stratum.getAmount() / 2));
                }
                parentGroup.subgroups.add(group);
            }
        }
    }

    void starve(double fraction) {
        if (subgroups.isEmpty()) {
            population -= (population / 10) * (1 - fraction) + 1;
            population = Math.max(population, 0);
        } else {
            int i = 0;
        }
        getCulturalCenter().addAspiration(new Aspiration(10, new AspectTag("food")));
    }

    void freeze(double fraction) {
        if (subgroups.isEmpty()) {
            population -= (population / 10) * (1 - fraction) + 1;
            population = Math.max(population, 0);
        } else {
            int i = 0;
        }
        getCulturalCenter().addAspiration(new Aspiration(10, new AspectTag("warmth")));
    }

    private boolean expand() {
        if (state == State.Dead || !ProbFunc.getChances(spreadability)) {
            return false;
        }
        if (population <= minPopulationPerTile * territory.size()) {
            List<Tile> t = new ArrayList<>();
            for (Group group : subgroups) {
                t.addAll(group.territory.getTiles());
            }
            removeTile(territory.excludeMostUselessTileExcept(t, tileValueMapper));
            if (population <= minPopulationPerTile * territory.size()) {
                removeTile(territory.excludeMostUselessTileExcept(t, tileValueMapper));
            }
        }

        claimTile(territory.includeMostUsefulTile(newTile -> newTile.group == null && newTile.canSettle(this) &&
                getDistanceToClosestSubgroup(newTile) < 4, tileValueMapper));
        return true;
    }

    private void claimTile(Tile tile) {
        if (tile == null) {
            return;
        }
        tile.group = this;
        territory.addTile(tile);
        addEvent(new Event(Event.Type.TileAquisition, getCulturalCenter().world.getTurn(), "Group " + name +
                " claimed tile " + tile.x + " " + tile.y, "group", this, "tile", tile));
    }

    private void removeTile(Tile tile) {
        if (tile == null) {
            return;
        }
        territory.removeTile(tile);
    }

    public void finishUpdate() {
        getCulturalCenter().finishUpdate();
        Tile tile = ProbFunc.randomTile(getOverallTerritory());
        resourcePack.disbandOnTile(tile);
        addEvent(new Event(Event.Type.DisbandResources, getCulturalCenter().world.getTurn(),
                "Resources were disbanded on tile " + tile.x + " " + tile.y,
                "tile", tile));
        if (subgroups.isEmpty()) {
            getAspects().forEach(Aspect::finishUpdate);
        }
        subgroups.forEach(Group::finishUpdate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Group group = (Group) o;
        return name.equals(group.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Group " + name + " is " + state +
                ", population=" + population + ", aspects:\n");
        for (Aspect aspect : getAspects()) {
            if (aspect.getUsefulness() < 0) {
                continue;
            }
            stringBuilder.append(aspect).append("\n\n");
        }
        stringBuilder.append("Aspirations: ");
        for (Aspiration aspiration : getCulturalCenter().getAspirations())
        {
            stringBuilder.append(aspiration).append(", ");
        }
        stringBuilder.append((culturalCenter.getAspirations().isEmpty() ? "none\n" : "\n"));
        stringBuilder.append("Requests: ");
        for (Request request : culturalCenter.getRequests()) {
            stringBuilder.append(request).append(", ");
        }
        stringBuilder.append((culturalCenter.getRequests().isEmpty() ? "none\n" : "\n"));
        StringBuilder s = new StringBuilder();
        s.append("Wants: ");
        for (ShnyPair<Resource, ResourceBehaviour> want : culturalCenter.getWants()) {
            s.append(want.first.getFullName()).append(" ").append(want.second).append(", ");
        }
        s.append((culturalCenter.getWants().isEmpty() ? "none\n" : "\n"));
        stringBuilder.append(s.toString());
        stringBuilder.append("Current resources:\n").append(cherishedResources).append("\n");
        stringBuilder.append("Artifacts:\n").append(uniqueArtefacts.toString())
                .append("\n");
        for (Stratum stratum: strata) {
            if (stratum.getAmount() != 0) {
                stringBuilder.append(stratum).append("\n");
            }
        }
        stringBuilder = OutputFunc.chompToSize(stringBuilder, 70);

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
