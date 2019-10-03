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
    private Group parentGroup;
    private CulturalCenter culturalCenter;
    private double spreadability;
    private int fertility, minPopulationPerTile;
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
        this.fertility = 10;
        this.minPopulationPerTile = 1;
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
        for (Aspect aspect : group.culturalCenter.getChangedAspects()) {
            getCulturalCenter().addAspectNow(aspect, aspect.getDependencies());
        }
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

    public int getFertility() {
        return fertility;
    }

    public Territory getTerritory() {
        return territory;
    }

    public List<Event> getEvents() {
        return getCulturalCenter().getEvents();
    }

    private void setAspects(Set<Aspect> aspects) {
        getCulturalCenter().setAspects(aspects);
    }

    private void setEvents(List<Event> events) {
        getCulturalCenter().setEvents(events);
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

    private void die() {
        state = State.Dead;
        population = 0;
        for (Tile tile : territory.getTiles()) {
            tile.group = null;
        }
        cherishedResources.disbandOnTile(territory.getTiles().get(ProbFunc.randomInt(territory.getTiles().size())));
        uniqueArtefacts.disbandOnTile(territory.getTiles().get(ProbFunc.randomInt(territory.getTiles().size())));
        addEvent(new Event(Event.Type.Death, getCulturalCenter().world.getTurn(), "Group " + name + " died", "group", this));
        for (Group group : culturalCenter.world.map.getAllNearGroups(this)) {
            group.culturalCenter.getMemePool().addMemeCombination(culturalCenter.world.getMemeFromPoolByName("group")
                    .addPredicate(new MemeSubject(name)).addPredicate(culturalCenter.world.getMemeFromPoolByName("die")));
        }
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

    private void addForConverseWrapper(ConverseWrapper converseWrapper, Map<AspectTag, Set<Dependency>> dep) { //TODO getRequirement rewritten with phony target
        List<Resource> _l = converseWrapper.resource.applyAspect(converseWrapper.aspect);
        if (!(_l.size() == 1 && _l.get(0).equals(converseWrapper.resource)) ||
                (converseWrapper instanceof MeaningInserter && converseWrapper.resource.hasApplicationForAspect(converseWrapper.aspect))) {
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

    public void update(double rAspectAcquisition, double rAspectLending) {
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

        for (Request request : getCulturalCenter().getRequests()) {
            List<ShnyPair<Aspect, Function<ResourcePack, Integer>>> pairs = getAspects().stream()
                    .map(aspect -> new ShnyPair<>(aspect, request.isAcceptable(aspect)))
                    .filter(pair -> pair.second != null).collect(Collectors.toList());
            for (ShnyPair<Aspect, Function<ResourcePack, Integer>> pair : pairs) {
                int amount = request.howMuchOfNeeded(resourcePack);
                if (amount > request.ceiling) {
                    break;
                }
                ShnyPair<Boolean, ResourcePack> _p = pair.first.use(request.ceiling - amount, pair.second);
                if (_p.first) {
                    resourcePack.add(_p.second);
                }
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
            }
        }
    }

    void starve(double fraction) {
        if (subgroups.isEmpty()) {
            population -= (population / 10) * (1 - fraction) + 1;
        }
        getCulturalCenter().addAspiration(new Aspiration(10, new AspectTag("food")));
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

        claimTile(territory.includeMostUsefulTile(newTile -> newTile.group == null && newTile.canSettle(this),
                tileValueMapper));
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
            stringBuilder.append(aspect).append("\n\n");
        }
        stringBuilder.append("Aspirations: ");
        for (Aspiration aspiration : getCulturalCenter().getAspirations()) {
            stringBuilder.append(aspiration.need.name).append(", ");
        }
        stringBuilder.append((culturalCenter.getAspirations().isEmpty() ? "none\n" : "\n"));
        stringBuilder.append("Requests: ");
        for (Request request : culturalCenter.getRequests()) {
            stringBuilder.append(request).append(", ");
        }
        stringBuilder.append((culturalCenter.getRequests().isEmpty() ? "none\n" : "\n"));
        stringBuilder.append("Current resources:\n").append(cherishedResources).append("\n");
        stringBuilder.append("Artifacts:\n").append(OutputFunc.chompToSize(uniqueArtefacts.toString(), 70))
                .append("\n");

        for (Group subgroup : subgroups) {
            stringBuilder = OutputFunc.addToRight(stringBuilder.toString(), subgroup.toString());
        }
        return stringBuilder.toString();
    }

    Group getParentGroup() {
        return parentGroup;
    }

    public enum State {
        Live,
        Dead
    }
}
