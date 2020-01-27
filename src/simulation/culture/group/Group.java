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

import static java.lang.Math.min;
import static simulation.Controller.*;

/**
 * Entity with Aspects, which develops through time.
 */
public class Group {
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
    private GroupConglomerate parentGroup;
    private CulturalCenter culturalCenter;
    private double spreadability;
    private Territory territory = new Territory();
    private Function<Tile, Integer> tileValueMapper = t -> t.getNeighbours(tile1 -> this.equals(tile1.group)).size() +
            3 * t.hasResources(getResourceRequirements());//TODO function is awful, it works inside out
    ResourcePack resourcePack = new ResourcePack();
    public ResourcePack cherishedResources = new ResourcePack();
    ResourcePack uniqueArtifacts = new ResourcePack();

    private Group(String name, int population, double spreadability, Tile root, GroupConglomerate parentGroup) {
        this.name = name;
        this.parentGroup = parentGroup;
        this.population = population;
        this.spreadability = spreadability;
        culturalCenter = new CulturalCenter(this);
        claimTile(root);
    }

    Group(GroupConglomerate group, Group subgroup, String name, int population, Tile tile) {
        this(name, population, session.defaultGroupSpreadability, tile, group);
        if (subgroup == null) {
            return;
        }
        for (Aspect aspect : subgroup.getAspects()) {
            culturalCenter.hardAspectAdd(aspect.copy(aspect.getDependencies(), this));
        }
        culturalCenter.getAspects().forEach(Aspect::swapDependencies);
        culturalCenter.getMemePool().addAll(subgroup.culturalCenter.getMemePool()); //TODO seems like memes can become unbinded with CultureAspects
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
        return getParentGroup().getOverallTerritory();
    }

    private Collection<Resource> getResourceRequirements() {
        return getAspects().stream().filter(aspect -> aspect instanceof ConverseWrapper)
                .map(aspect -> ((ConverseWrapper) aspect).resource).distinct().collect(Collectors.toList());
    }

    public GroupConglomerate getParentGroup() {
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
            if (requirement.name.equals(AspectTag.phony().name)) {
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
                Dependency dependency = new AspectDependency(requirement, selfAspect);
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
        getCulturalCenter().getEvents().add(event);
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
            amount = min(amount, getFreePopulation());
        }
        stratum.useAmount(amount);
        return amount;
    }

    void updateRequests() {
        if (territory.isEmpty()) {
            int i = 0;
        }
        getCulturalCenter().updateRequests();
    }

    void executeRequests() {
        for (Request request : getCulturalCenter().getRequests()) { //TODO do smth about getting A LOT MORE resources than planned due to one to many resource conversion
            List<ShnyPair<Stratum, ResourceEvaluator>> pairs = strata.stream()
                    .map(stratum -> new ShnyPair<>(stratum, request.isAcceptable(stratum)))
                    .filter(pair -> pair.second != null).sorted(Comparator.comparingInt(pair ->
                            request.satisfactionLevel(pair.first)))
                    .collect(Collectors.toList());
            for (ShnyPair<Stratum, ResourceEvaluator> pair : pairs) {
                int amount = pair.second.evaluate(resourcePack);
                if (amount >= request.ceiling) {
                    break;
                }
                resourcePack.add(pair.first.use(request.ceiling - amount, pair.second));
            }
            request.end(resourcePack);
        }
    }

    void strataUpdate() {
        strata.forEach(Stratum::update);
    }

    void populationUpdate() {
        if (population == 0) {
            die();
            return;
        }
        if ((getMaxPopulation() == population || ProbFunc.getChances(session.defaultGroupDiverge))
                && parentGroup.subgroups.size() < 10) {
            List<Tile> tiles = getOverallTerritory().getBrinkWithCondition(t -> t.group == null &&
                    parentGroup.getClosestInnerGroupDistance(t) > 2 && t.canSettle(this));
            if (tiles.isEmpty()) {
                return;
            }
            if (!session.groupMultiplication) {
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
            group.culturalCenter.initializeFromCenter(culturalCenter);//TODO maybe put in constructor somehow
            parentGroup.addGroup(group);
        }
    }

    public void finishUpdate() {
        getCulturalCenter().finishUpdate();
        Tile tile = ProbFunc.randomTile(getOverallTerritory());
        resourcePack.disbandOnTile(tile);
        addEvent(new Event(Event.Type.DisbandResources, "Resources were disbanded on tile " + tile.x + " " +
                tile.y, "tile", tile));
    }

    void starve(double fraction) {
        decreasePopulation((population / 10) * (1 - fraction) + 1);
        getCulturalCenter().addAspiration(new Aspiration(10, new AspectTag("food")));
    }

    void freeze(double fraction) {
        decreasePopulation((population / 10) * (1 - fraction) + 1);
        getCulturalCenter().addAspiration(new Aspiration(10, new AspectTag("warmth")));
    }

    private void decreasePopulation(double amount) {
        decreasePopulation((int) amount);
    }

    private void decreasePopulation(int amount) {
        if (getFreePopulation() < 0) {
            int i = 0; //TODO still happens
        }
        amount = min(population, amount);
        int delta = amount - getFreePopulation();
        if (delta > 0) {
            for (Stratum stratum: strata) {
                int part = (int) min(amount * (((double) stratum.getAmount()) / population) + 1, stratum.getAmount());
                stratum.freeAmount(part);
            }
        }
        population -= amount;
        if (getFreePopulation() < 0) {
            int i = 0; //TODO still happens
        }
    }

    boolean diverge() {
        if (!session.groupDiverge) {//TODO diverge if too far from others
            return false;
        }
        if (parentGroup.subgroups.size() > 1 && ProbFunc.getChances(session.defaultGroupExiting)) {
            parentGroup.removeGroup(this);
            GroupConglomerate group = new GroupConglomerate(0, getCenter());
            group.addGroup(this);
            parentGroup = group;
            session.world.addGroup(group);
            return true;
        }
        return false;
    }

    boolean expand() {
        if (state == State.Dead) {
            return false;
        }
        if (!ProbFunc.getChances(spreadability)) {
            return false;
        }
        if (population <= minPopulationPerTile * territory.size()) {
            parentGroup.getTerritory().removeTile(territory.excludeMostUselessTileExcept(new ArrayList<>(), tileValueMapper));
            if (population <= minPopulationPerTile * territory.size()) {
                parentGroup.getTerritory().removeTile(territory.excludeMostUselessTileExcept(new ArrayList<>(), tileValueMapper));
            }
        }

        claimTile(territory.getMostUsefulTile(newTile -> newTile.group == null && newTile.canSettle(this) &&
                isTileReachable(newTile), tileValueMapper));
        return true;
    }

    boolean migrate() {
        if (state == State.Dead) {
            return false;
        }//TODO migration
        if (!shouldMigrate()) {
            return false;
        }

        Tile newCenter = getMigrationTile();
        if (newCenter == null) {
            return false;
        }
        territory.setCenter(newCenter);
        claimTile(newCenter);
        removeTiles(territory.getTilesWithPredicate(tile -> !isTileReachable(tile)));
        return true;
    }

    private Tile getMigrationTile() {
        return territory.getCenter().getNeighbours().stream()
                .max(Comparator.comparingInt(tile -> tileValueMapper.apply(tile))).orElse(null);
        //return ProbFunc.randomElement(territory.getCenter().getNeighbours(tile -> tile.canSettle(this)));
    }

    private boolean shouldMigrate() {
        return !culturalCenter.getAspirations().isEmpty();
    }

    private void claimTile(Tile tile) {
        if (tile == null) {
            return;
        }
        parentGroup.claimTile(tile);
        tile.group = this;
        territory.add(tile);
        addEvent(new Event(Event.Type.TileAcquisition, "Group " + name + " claimed tile " + tile.x + " " +
                tile.y, "group", this, "tile", tile));
    }

    private void removeTiles(Collection<Tile> tiles) {
        tiles.forEach(this::removeTile);
    }

    private void removeTile(Tile tile) {
        if (tile == null) {
            return;
        }
        territory.removeTile(tile);
        parentGroup.removeTile(tile);
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
        for (Stratum stratum : strata) {
            if (stratum.getAmount() != 0) {
                stringBuilder.append(stratum).append("\n");
            }
        }
        stringBuilder = OutputFunc.chompToSize(stringBuilder, 70);

        return stringBuilder.toString();
    }

    public enum State {
        Live,
        Dead
    }
}
