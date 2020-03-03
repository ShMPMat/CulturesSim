package simulation.culture.group;

import extra.OutputFunc;
import static shmp.random.RandomProbabilitiesKt.testProbability;
import static shmp.random.RandomCollectionsKt.*;
import extra.ShnyPair;
import extra.SpaceProbabilityFuncs;
import simulation.culture.Event;
import simulation.culture.aspect.*;
import simulation.culture.aspect.dependency.*;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.group.intergroup.Relation;
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
    private TerritoryCenter territoryCenter = new TerritoryCenter(this);
    private Function<Group, Double> hostilityByDifferenceCoef = g -> (Groups.getGroupsDifference(this, g) - 1) / 2;
    ResourcePack resourcePack = new ResourcePack();
    public ResourcePack cherishedResources = new ResourcePack();
    ResourcePack uniqueArtifacts = new ResourcePack();

    private Group(String name, int population, double spreadability, Tile root, GroupConglomerate parentGroup) {
        this.name = name;
        this.parentGroup = parentGroup;
        this.population = population;
        this.spreadability = spreadability;
        culturalCenter = new CulturalCenter(this);
        territoryCenter.claimTile(root);
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
        culturalCenter.getMemePool().addAll(subgroup.culturalCenter.getMemePool());
    }

    public Set<Aspect> getAspects() {
        return getCulturalCenter().getAspects();
    }

    public Aspect getAspect(Aspect aspect) {
        return culturalCenter.getAspect(aspect);
    }

    double getSpreadability() {
        return spreadability;
    }

    int getMinPopulationPerTile() {
        return minPopulationPerTile;
    }

    public List<Tile> getTiles() {
        return getTerritory().getTiles();
    }

    public CulturalCenter getCulturalCenter() {
        return culturalCenter;
    }

    TerritoryCenter getTerritoryCenter() {
        return territoryCenter;
    }

    int getFertility() {
        return fertility;
    }

    public Territory getTerritory() {
        return territoryCenter.getTerritory();
    }

    List<Stratum> getStrata() {
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

    Collection<Resource> getResourceRequirements() {
        return getAspects().stream().filter(aspect -> aspect instanceof ConverseWrapper)
                .map(aspect -> ((ConverseWrapper) aspect).resource).distinct().collect(Collectors.toList());
    }

    public GroupConglomerate getParentGroup() {
        return parentGroup;
    }

    void setParentGroup(GroupConglomerate parentGroup) {
        this.parentGroup = parentGroup;
    }

    private void die() {
        state = State.Dead;
        population = 0;
        for (Tile tile : getTiles()) {
            tile.group = null;
        }
        cherishedResources.disbandOnTile(randomElement(getTiles(), session.random));
        uniqueArtifacts.disbandOnTile(randomElement(getTiles(), session.random));
        addEvent(new Event(Event.Type.Death, "Group " + name + " died", "group", this));
        for (Group group : Groups.getAllNearGroups(this)) {
            group.culturalCenter.addMemeCombination(session.world.getPoolMeme("group")
                    .addPredicate(new MemeSubject(name)).addPredicate(session.world.getPoolMeme("die")));
        }
    }

    public void addEvent(Event event) {
        getCulturalCenter().getEvents().add(event);
    }

    public Tile getCenter() {
        return getTerritory().getCenter();
    }

    public int changeStratumAmountByAspect(Aspect aspect, int amount) {
        Stratum stratum = getStratumByAspect(aspect);
        try {
            if (stratum.getAmount() < amount) {
                amount = min(amount, getFreePopulation());
            }
            stratum.useAmount(amount);
            return amount;
        } catch (NullPointerException e) {
            throw new RuntimeException("No stratum for Aspect");
        }
    }

    void updateRequests() {
        if (getTerritory().isEmpty()) {
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
                resourcePack.add(pair.first.use(new AspectController(request.ceiling - amount, request.floor,
                        pair.second)));
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
        if ((getMaxPopulation() == population || testProbability(session.defaultGroupDiverge, session.random))
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
            Tile tile = randomElement(tiles, session.random);
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
            if (group.getTerritory().isEmpty()) {
                int i = 0;
            }
            group.culturalCenter.initializeFromCenter(culturalCenter);//TODO maybe put in constructor somehow
            parentGroup.addGroup(group);
        }
    }

    void intergroupUpdate() {
        Map<Group, Relation> relations = culturalCenter.relations;
        if (session.isTime(session.groupTurnsBetweenBorderCheck)) {
            List<Group> groups = getOverallTerritory()
                    .getBrinkWithCondition(tile -> tile.group != null && tile.group.parentGroup != parentGroup)
                    .stream().map(tile -> tile.group).distinct().collect(Collectors.toList());
            for (Group group: groups) {
                if (!relations.containsKey(group)) {
                    Relation relation = new Relation(this, group);
                    relation.setPair(group.addMirrorRelation(relation));
                    relations.put(relation.other, relation);
                }
            }
            List<Group> dead = new ArrayList<>();
            for (Relation relation: relations.values()) {
                if (relation.other.state == State.Dead) {
                    dead.add(relation.other);
                } else {
                    relation.setPositive(hostilityByDifferenceCoef.apply(relation.other));
                }
            }
            dead.forEach(relations::remove);
        }
        culturalCenter.intergroupUpdate();
    }

    Relation addMirrorRelation(Relation relation) {
        Relation newRelation = new Relation(relation.other, relation.owner);
        newRelation.setPair(relation);
        culturalCenter.relations.put(relation.owner, newRelation);
        return newRelation;
    }

    public void finishUpdate() {
        getCulturalCenter().finishUpdate();
        Tile tile = getDisbandTile();
        resourcePack.disbandOnTile(tile);
        addEvent(new Event(Event.Type.DisbandResources, "Resources were disbanded on tile " + tile.x + " " +
                tile.y, "tile", tile));
    }

    public Tile getDisbandTile() {
        return SpaceProbabilityFuncs.randomTile(getOverallTerritory());
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
        if (!session.groupDiverge) {
            return false;
        }
        if (parentGroup.subgroups.size() > 1 && testProbability(session.defaultGroupExiting, session.random)) {
            if (checkCoherencyAndDiverge()) {
                createNewConglomerate(Collections.singleton(this));
            }
            return true;
        }
        return false;
    }

    private boolean checkCoherencyAndDiverge() {
        Queue<Group> queue = new ArrayDeque<>();
        queue.add(this);
        Set<Group> cluster = new HashSet<>();
        while (!queue.isEmpty()) {
            Group cur = queue.poll();
            cluster.add(cur);
            queue.addAll(cur.getTerritory().getBorder().stream()
                    .filter(t -> t.group != null && t.group.parentGroup == parentGroup && !cluster.contains(t.group))
                    .map(tile -> tile.group).collect(Collectors.toList()));
        }
        if (parentGroup.subgroups.size() == cluster.size()) {
            return false;
        }
        createNewConglomerate(cluster);
        return true;
    }

    private void createNewConglomerate(Collection<Group> groups) {
        GroupConglomerate conglomerate = new GroupConglomerate(0, getCenter());
        for (Group group: groups) {
            parentGroup.removeGroup(group);
            conglomerate.addGroup(group);
        }
        session.world.addGroup(conglomerate);
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
        stringBuilder.append("Current resources:\n").append(cherishedResources).append("\n\n");
        stringBuilder.append("Artifacts:\n").append(uniqueArtifacts.toString())
                .append("\n\n");
        for (Stratum stratum : strata) {
            if (stratum.getAmount() != 0) {
                stringBuilder.append(stratum).append("\n");
            }
        }
        stringBuilder.append("\n");
        for (Relation relation: culturalCenter.relations.values()) {
            stringBuilder.append(relation).append("\n");
        }
        stringBuilder = OutputFunc.chompToSize(stringBuilder, 70);

        return stringBuilder.toString();
    }

    public enum State {
        Live,
        Dead
    }
}
