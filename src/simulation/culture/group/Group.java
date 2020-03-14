package simulation.culture.group;

import extra.OutputFunc;

import static shmp.random.RandomProbabilitiesKt.testProbability;
import static shmp.random.RandomCollectionsKt.*;

import simulation.Event;
import simulation.culture.aspect.*;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.group.intergroup.Relation;
import simulation.culture.group.request.Request;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.Territory;
import simulation.space.Tile;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static simulation.Controller.*;

public class Group {
    public State state = State.Live;
    public String name;

    private int fertility = session.defaultGroupFertility;
    private GroupConglomerate parentGroup;
    private CultureCenter cultureCenter;
    private TerritoryCenter territoryCenter;
    private PopulationCenter populationCenter;
    private Function<Group, Double> hostilityByDifferenceCoef = g -> (Groups.getGroupsDifference(this, g) - 1) / 2;
    ResourcePack resourcePack = new ResourcePack();
    public ResourcePack cherishedResources = new ResourcePack();
    ResourcePack uniqueArtifacts = new ResourcePack();

    Group(
            GroupConglomerate parentGroup,
            String name,
            PopulationCenter populationCenter,
            Tile tile,
            List<Aspect> aspects,
            GroupMemes memePool,
            double spreadAbility
    ) {
        this.name = name;
        this.parentGroup = parentGroup;
        this.populationCenter = populationCenter;
        cultureCenter = new CultureCenter(this, memePool, aspects);
        territoryCenter = new TerritoryCenter(this, spreadAbility, tile);
    }

    public CultureCenter getCultureCenter() {
        return cultureCenter;
    }

    public TerritoryCenter getTerritoryCenter() {
        return territoryCenter;
    }

    public PopulationCenter getPopulationCenter() {
        return populationCenter;
    }

    public Territory getTerritory() {
        return territoryCenter.getTerritory();
    }

    public Territory getOverallTerritory() {
        return getParentGroup().getTerritory();
    }

    public GroupConglomerate getParentGroup() {
        return parentGroup;
    }

    void setParentGroup(GroupConglomerate parentGroup) {
        this.parentGroup = parentGroup;
    }

    private void die() {
        state = State.Dead;
        populationCenter.die();
        territoryCenter.die();
        addEvent(new Event(Event.Type.Death, "Group " + name + " died", "group", this));
        for (Group group : territoryCenter.getAllNearGroups(this)) {
            group.cultureCenter.addMemeCombination(session.world.getPoolMeme("group")
                    .addPredicate(new MemeSubject(name)).addPredicate(session.world.getPoolMeme("die")));
        }
    }

    public void addEvent(Event event) {
        getCultureCenter().getEvents().add(event);
    }

    void update() {
        cultureCenter.updateRequests(populationCenter.getPopulation() / fertility + 1);
        populationCenter.executeRequests(getCultureCenter().getRequests());
        getPopulationCenter().strataUpdate();
        if (state != State.Dead) {
            if (shouldMigrate()) {
                territoryCenter.migrate();
            }
            if (populationCenter.isMinPassed(territoryCenter.getTerritory())) {
                territoryCenter.expand();
            } else {
                territoryCenter.shrink();
            }
            getCultureCenter().update();
        }
    }

    private boolean shouldMigrate() {
        return !cultureCenter.getAspirations().isEmpty();
    }

    Group populationUpdate() {
        if (populationCenter.getPopulation() == 0) {
            die();
            return null;
        }
        if ((populationCenter.isMaxReached(territoryCenter.getTerritory())
                || testProbability(session.defaultGroupDiverge, session.random))
                && parentGroup.subgroups.size() < 10) {
            List<Tile> tiles = getOverallTerritory().getBrink(t -> territoryCenter.canSettle(
                    t,
                    t2 -> t2.group == null && parentGroup.getClosestInnerGroupDistance(t2) > 2
            ));
            if (tiles.isEmpty()) {
                return null;
            }
            if (!session.groupMultiplication) {
                return null;
            }
            Tile tile = randomElement(tiles, session.random);
            List<Aspect> aspects = cultureCenter.getAspectCenter().getAspectPool().getAll().stream()
                    .map(a -> a.copy(a.getDependencies(), this))
                    .collect(Collectors.toList());
            GroupMemes memes = new GroupMemes();
            memes.addAll(cultureCenter.getMemePool());
            return new Group(
                    parentGroup,
                    parentGroup.name + "_" + parentGroup.subgroups.size(),
                    populationCenter.getPart(0.5),
                    tile,
                    aspects,
                    memes,
                    territoryCenter.getSpreadAbility()
            );
        }
        return null;
    }

    void intergroupUpdate() {
        Map<Group, Relation> relations = cultureCenter.relations;
        if (session.isTime(session.groupTurnsBetweenBorderCheck)) {
            List<Group> groups = getOverallTerritory()
                    .getBrink(tile -> tile.group != null && tile.group.parentGroup != parentGroup)
                    .stream().map(tile -> tile.group).distinct().collect(Collectors.toList());
            for (Group group : groups) {
                if (!relations.containsKey(group)) {
                    Relation relation = new Relation(this, group);
                    relation.setPair(group.addMirrorRelation(relation));
                    relations.put(relation.other, relation);
                }
            }
            List<Group> dead = new ArrayList<>();
            for (Relation relation : relations.values()) {
                if (relation.other.state == State.Dead) {
                    dead.add(relation.other);
                } else {
                    relation.setPositive(hostilityByDifferenceCoef.apply(relation.other));
                }
            }
            dead.forEach(relations::remove);
        }
        cultureCenter.intergroupUpdate();
    }

    Relation addMirrorRelation(Relation relation) {
        Relation newRelation = new Relation(relation.other, relation.owner);
        newRelation.setPair(relation);
        cultureCenter.relations.put(relation.owner, newRelation);
        return newRelation;
    }

    public void finishUpdate() {
        getCultureCenter().finishUpdate();
    }

    void starve(double fraction) {
        populationCenter.decreasePopulation((int) ((populationCenter.getPopulation() / 10) * (1 - fraction) + 1));
        getCultureCenter().addAspiration(new Aspiration(10, new ResourceTag("food")));
    }

    void freeze(double fraction) {
        populationCenter.decreasePopulation((int) ((populationCenter.getPopulation() / 10) * (1 - fraction) + 1));
        getCultureCenter().addAspiration(new Aspiration(10, new ResourceTag("warmth")));
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
            queue.addAll(cur.getTerritoryCenter().getTerritory().getBorder().stream()
                    .filter(t -> t.group != null && t.group.parentGroup == parentGroup && !cluster.contains(t.group))
                    .map(tile -> tile.group)
                    .collect(Collectors.toList()));
        }
        if (parentGroup.subgroups.size() == cluster.size()) {
            return false;
        }
        createNewConglomerate(cluster);
        return true;
    }

    private void createNewConglomerate(Collection<Group> groups) {
        GroupConglomerate conglomerate = new GroupConglomerate(
                0,
                getTerritoryCenter().getTerritory().getCenter()
        );
        for (Group group : groups) {
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
                ", population=" + populationCenter.getPopulation() + ", aspects:\n");
        for (Aspect aspect : cultureCenter.getAspectCenter().getAspectPool().getAll()) {
            stringBuilder.append(aspect).append("\n\n");
        }
        stringBuilder.append("Aspirations: ");
        for (Aspiration aspiration : getCultureCenter().getAspirations()) {
            stringBuilder.append(aspiration).append(", ");
        }
        stringBuilder.append((cultureCenter.getAspirations().isEmpty() ? "none\n" : "\n"));
        stringBuilder.append("Requests: ");
        for (Request request : cultureCenter.getRequests()) {
            stringBuilder.append(request).append(", ");
        }
        stringBuilder.append((cultureCenter.getRequests().isEmpty() ? "none\n" : "\n"));
        StringBuilder s = new StringBuilder();
        s.append("Aspects: ");
        for (CultureAspect aspect : cultureCenter.getCultureAspectCenter().getAspectPool().getAll()) {
            s.append(aspect).append(", ");
        }
        s.append((cultureCenter.getCultureAspectCenter().getAspectPool().isEmpty() ? "none\n" : "\n"));
        stringBuilder.append(s.toString());
        stringBuilder.append("Current resources:\n").append(cherishedResources).append("\n\n");
        stringBuilder.append("Artifacts:\n").append(uniqueArtifacts.toString())
                .append("\n\n");
        for (Stratum stratum : populationCenter.getStrata()) {
            if (stratum.getAmount() != 0) {
                stringBuilder.append(stratum).append("\n");
            }
        }
        stringBuilder.append("\n");
        for (Relation relation : cultureCenter.relations.values()) {
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
