package simulation.culture.group;

import extra.OutputFunc;
import extra.ProbFunc;
import extra.ShnyPair;
import simulation.culture.Event;
import simulation.culture.aspect.*;
import simulation.culture.aspect.dependency.*;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.Territory;
import simulation.space.Tile;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static simulation.Controller.*;

/**
 * Entity with Aspects, which develops through time.
 */
public class Group {
    public char type; //TODO CRUTCH; = O if overgroup, = S if subgroup

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
    private Group parentGroup;
    private CulturalCenter culturalCenter = new CulturalCenter(this);;
    private double spreadability;
    private Territory territory = new Territory();
    private Function<Tile, Integer> tileValueMapper = t -> t.getNeighbours(tile1 -> this.equals(tile1.group)).size() -
            3 * t.closestTileWithResources(getResourceRequirements());
    ResourcePack resourcePack = new ResourcePack();
    public ResourcePack cherishedResources = new ResourcePack();
    ResourcePack uniqueArtifacts = new ResourcePack();

    private Group(String name, int population, double spreadability, int numberOfSubGroups, Tile root, Group parentGroup) {
        this.name = name;
        this.parentGroup = parentGroup;
        this.population = population;
        this.spreadability = spreadability;
        if (root == null) {
            while (true) {
                Tile tile = ProbFunc.randomTile(session.world.map);
                if (tile.group == null && tile.canSettle(this)) {
                    overgroupClaim(tile);
                    break;
                }
            }
        } else {
            claimTile(root);
        }

        for (int i = 0; i < numberOfSubGroups; i++) {
            overgroupAddSubgroup(new Group(this, null, name + "_" + i, population / numberOfSubGroups,
                    getCenter()));
        }
    }

    public Group(int numberOfSubgroups, Tile root) {
        this(session.getVacantGroupName(), 100 + ProbFunc.randomInt(100),
                session.defaultGroupSpreadability, numberOfSubgroups, root,null);
        type = 'O';
    }

    private Group(Group group, Group subgroup, String name, int population, Tile tile) {
        this(name, population, session.defaultGroupSpreadability, 0, tile, group);
        if (subgroup == null) {
            return;
        }
        for (Aspect aspect : subgroup.getAspects()) {
            getCulturalCenter().addAspectNow(aspect, aspect.getDependencies());
            overgroupFinishUpdate();
        }
        culturalCenter.getMemePool().addAll(subgroup.culturalCenter.getMemePool());
        type = 'S';
    }

    Set<Aspect> getAspects() {
        return getCulturalCenter().getAspects();
    }

    public Set<Aspect> getAspects2() {
        return subgroups.isEmpty() ? getCulturalCenter().getAspects() : subgroups.get(0).getCulturalCenter().getAspects();
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
        if (getParentGroup() == null) {
            int i = 0;//TODO fuck.
            return this;
        }
        return getParentGroup().getOverallGroup();
    }

    private Collection<Resource> getResourceRequirements() {
        return getAspects().stream().filter(aspect -> aspect instanceof ConverseWrapper)
                .map(aspect -> ((ConverseWrapper) aspect).resource).distinct().collect(Collectors.toList());
    }

    Group getParentGroup() {
        return parentGroup;
    }

    private void die() {
        state = State.Dead;
        population = 0;
        for (Tile tile : territory.getTiles()) {
            tile.group = null;
        }
        cherishedResources.disbandOnTile(ProbFunc.randomElement(territory.getTiles()));
        uniqueArtifacts.disbandOnTile(ProbFunc.randomElement(territory.getTiles()));
        addEvent(new Event(Event.Type.Death, "Group " + name + " died", "group", this));
        for (Group group : session.world.map.getAllNearGroups(this)) {
            group.culturalCenter.addMemeCombination(session.world.getPoolMeme("group")
                    .addPredicate(new MemeSubject(name)).addPredicate(session.world.getPoolMeme("die")));
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

    private void addForConverseWrapper(ConverseWrapper converseWrapper, Map<AspectTag, Set<Dependency>> dep) {
        if (converseWrapper.resource.hasApplicationForAspect(converseWrapper.aspect)) {
            if (getOverallTerritory().getDifferentResources().contains(converseWrapper.resource)) {
                addDependenciesInMap(dep, Collections.singleton(/*new Dependency_(converseWrapper.getRequirement(), this,
                        new ShnyPair<>(converseWrapper.resource, converseWrapper.aspect))*/
                new ConversionDependency(converseWrapper.getRequirement(), this,
                        new ShnyPair<>(converseWrapper.resource, converseWrapper.aspect))), converseWrapper.getRequirement());
            }
            addDependenciesInMap(dep, getCulturalCenter().getAllProducedResources().stream()
                            .filter(pair -> pair.first.equals(converseWrapper.resource))
                            .map(pair -> /*new Dependency_(converseWrapper.getRequirement(), new ShnyPair<>(converseWrapper, pair.second),
                                    this)*/
                            new LineDependency(converseWrapper.getRequirement(), this,
                                    new ShnyPair<>(converseWrapper, pair.second))).filter(dependency ->
                                    !dependency.isCycleDependency(converseWrapper)).collect(Collectors.toList()),
                    converseWrapper.getRequirement());
        }
    }

    private void addResourceDependencies(AspectTag requirement, Map<AspectTag, Set<Dependency>> dep) {
        List<Resource> _r = territory.getResourcesWithAspectTag(requirement);
        if (_r != null) {
            addDependenciesInMap(dep, _r.stream().map(resource -> /*new Dependency_(requirement, this, resource)*/
                    new ResourceDependency(requirement, this, resource))
                    .collect(Collectors.toList()), requirement);
        }
    }

    private void addAspectDependencies(AspectTag requirement, Map<AspectTag, Set<Dependency>> dep) {
        for (Aspect selfAspect : getAspects()) {
            if (selfAspect.getTags().contains(requirement)) {
                Dependency dependency = /*new Dependency_(requirement, this, selfAspect);*/ new AspectDependency(requirement, selfAspect);
                if (dependency.isCycleDependency(selfAspect)) {
                    continue;
                }
                addDependenciesInMap(dep, Collections.singleton(dependency), requirement);
            }
//            addDependenciesInMap(dep, culturalCenter.getAllProducedResources().stream()
//                    .filter(pair -> pair.first.));
            addDependenciesInMap(dep, territory.getResourcesWhichConverseToTag(selfAspect, requirement).stream() //Make converse Dependency_
                            .map(resource -> /*new Dependency_(requirement, this, new ShnyPair<>(resource, selfAspect))*/
                            new ConversionDependency(requirement, this, new ShnyPair<>(resource, selfAspect)))
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

    public Tile getCenter() {
        return territory.getCenter();
    }

    private boolean isTileReachable(Tile tile) {
        return getCenter().getDistance(tile) < 4;
    }

    public int changeStratumAmountByAspect(Aspect aspect, int amount) {
        Stratum stratum = getStratumByAspect(aspect);
        if (stratum.getAmount() < amount) {
            amount = Math.min(amount, getFreePopulation());
        }
        stratum.useAmount(amount);
        return amount;
    }

    private void updateRequests() {
        if (territory.isEmpty()) {
            int i = 0;
        }
        getCulturalCenter().updateRequests();
    }

    private void executeRequests() {
        for (Request request : getCulturalCenter().getRequests()) { //TODO do smth about getting A LOT MORE resources than planned due to one to many resource conversion
            List<ShnyPair<Stratum, ResourceEvaluator>> pairs = strata.stream()
                    .map(stratum -> new ShnyPair<>(stratum, request.isAcceptable(stratum)))
                    .filter(pair -> pair.second != null).sorted(Comparator.comparingInt(pair ->
                            request.satisfactionLevel(pair.first)))
                    .collect(Collectors.toList());
            for (ShnyPair<Stratum, ResourceEvaluator> pair : pairs) {
                int amount = pair.second.evaluate(resourcePack);
                if (amount > request.ceiling) {
                    break;
                }
                resourcePack.add(pair.first.use(request.ceiling - amount, pair.second));
            }
            request.end(resourcePack);
        }
    }

    private void strataUpdate() {
        strata.forEach(Stratum::update);
    }

    private void populationUpdate() {
        if (population == 0) {
            die();
            return;
        }
        if (getMaxPopulation() == population && parentGroup.subgroups.size() < 10) {
            List<Tile> tiles = getOverallTerritory().getBrinkWithCondition(t -> t.group == null &&
                    parentGroup.overgroupGetDistanceToClosestSubgroup(t) > 2 && t.canSettle(this));
            if (tiles.isEmpty()) {
                return;
            }
            if (!session.subgroupMultiplication) {
                return;
            }
            population = population / 2;
            Tile tile = ProbFunc.randomElement(tiles);
            Group group = new Group(parentGroup, this, parentGroup.name + "_" + parentGroup.subgroups.size(),
                    population, tile);
            for (Stratum stratum: strata) {
                try {
                    group.strata.get(group.strata.indexOf(stratum)).useAmount(stratum.getAmount() / 2);
                } catch (Exception e) {
                    int i = 0;
                }
                stratum.useAmount(stratum.getAmount() - (stratum.getAmount() / 2));
            }
            if (group.territory.isEmpty()) {
                int i = 0;
            }
            parentGroup.overgroupAddSubgroup(group);
        }
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
        subgroups.forEach(group -> group.culturalCenter.update());
        getCulturalCenter().overgroupUpdate();
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
            die();
        }
    }

    private void overgroupComputePopulation() {
        population = 0;
        for (Group subgroup : subgroups) {
            population += subgroup.population;
        }
    }

    private void overgroupClaim(Tile tile) {
        if (tile.group != null) {
            int i = 0;
        }
        tile.group = this;
        territory.add(tile);
    }

    private void overgroupRemove(Tile tile) {
        tile.group = null;
        territory.removeTile(tile);
    }

    private void overgroupAddSubgroup(Group subgroup) {
        subgroups.add(subgroup);
        overgroupComputePopulation();
    }

    private int overgroupGetDistanceToClosestSubgroup(Tile tile) {
        int d = Integer.MAX_VALUE;
        for (Group subgroup: subgroups) {
            d = Math.min(d, tile.getClosestDistance(Collections.singleton(subgroup.getCenter())));
        }
        return d;
    }

    private void overgroupRemoveSubgroup(Group subgroup) {
        population -= subgroup.population;
        if (!subgroups.remove(subgroup)) {
            System.err.println("Trying to remove non-child subgroup " + subgroup.name + " from Group " + name);
        }
    }

    public void overgroupFinishUpdate() {
        getCulturalCenter().finishUpdate();
        Tile tile = ProbFunc.randomTile(getOverallTerritory());
        resourcePack.disbandOnTile(tile);
        addEvent(new Event(Event.Type.DisbandResources, "Resources were disbanded on tile " + tile.x + " " +
                tile.y, "tile", tile));
        if (subgroups.isEmpty()) {
            getAspects().forEach(Aspect::finishUpdate);
        }
        subgroups.forEach(Group::overgroupFinishUpdate);
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

    private boolean diverge() {//TODO make diverge for single subgroup groups
        if (!session.groupDiverge) {
            return false;
        }
        if (population == getMaxPopulation() && parentGroup.subgroups.size() > 1 && ProbFunc.getChances(session.defaultGroupDiverge)) {
            parentGroup.overgroupRemoveSubgroup(this);
            Group group = new Group(0, getCenter());
            group.overgroupAddSubgroup(this);
            territory.getTiles().forEach(parentGroup::overgroupRemove);
            territory.getTiles().forEach(group::overgroupClaim);
            parentGroup = group;
            for (Aspect aspect : getAspects()) {
                group.getCulturalCenter().addAspectNow(aspect.copy(aspect.getDependencies(), group), aspect.getDependencies());
                group.overgroupFinishUpdate();
            }
            session.world.addGroup(group);
            return true;
        }
        return false;
    }

    private boolean expand() {//TODO differentiate groups and overgroups
        if (state == State.Dead) {
            return false;
        }
        if (!ProbFunc.getChances(spreadability)) {
            return false;
        }
        if (population <= minPopulationPerTile * territory.size()) {
            parentGroup.territory.removeTile(territory.excludeMostUselessTileExcept(new ArrayList<>(), tileValueMapper));
            if (population <= minPopulationPerTile * territory.size()) {
                parentGroup.territory.removeTile(territory.excludeMostUselessTileExcept(new ArrayList<>(), tileValueMapper));
            }
        }

        claimTile(territory.includeMostUsefulTile(newTile -> newTile.group == null && newTile.canSettle(this) &&
                isTileReachable(newTile), tileValueMapper));
        return true;
    }

    private boolean migrate() {
        if (state == State.Dead) {
            return false;
        }//TODO migration

//        claimTile(territory.includeMostUsefulTile(newTile -> newTile.group == null && newTile.canSettle(this) &&
//                isTileReachable(newTile), tileValueMapper));
        return true;
    }

    private void claimTile(Tile tile) {
        if (!subgroups.isEmpty()) {
            int i = 0;
        }
        if (tile == null) {
            return;
        }
        if (parentGroup != null) {
            parentGroup.overgroupClaim(tile);
        }
        if (tile.group != parentGroup) {
            int i = 0;
        }
        territory.add(tile);
        addEvent(new Event(Event.Type.TileAcquisition, "Group " + name + " claimed tile " + tile.x + " " +
                tile.y, "group", this, "tile", tile));
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
        s.append("Aspects: ");
        for (CultureAspect aspect : culturalCenter.getCultureAspects()) {
            s.append(aspect).append(", ");
        }
        s.append((culturalCenter.getCultureAspects().isEmpty() ? "none\n" : "\n"));
        stringBuilder.append(s.toString());
        stringBuilder.append("Current resources:\n").append(cherishedResources).append("\n");
        stringBuilder.append("Artifacts:\n").append(uniqueArtifacts.toString())
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
