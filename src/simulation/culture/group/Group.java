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
import simulation.space.tile.Tile;
import simulation.space.resource.MutableResourcePack;

import java.util.*;
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
    private RelationCenter relationCenter;
    MutableResourcePack resourcePack = new MutableResourcePack();
    public MutableResourcePack cherishedResources = new MutableResourcePack();
    MutableResourcePack uniqueArtifacts = new MutableResourcePack();

    Group(
            GroupConglomerate parentGroup,
            String name,
            PopulationCenter populationCenter,
            RelationCenter relationCenter,
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
        this.relationCenter = relationCenter;
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

    public RelationCenter getRelationCenter() {
        return relationCenter;
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
            group.cultureCenter.getMemePool().addMemeCombination(session.world.getPoolMeme("group")
                    .addPredicate(new MemeSubject(name)).addPredicate(session.world.getPoolMeme("die")));
        }
    }

    public void addEvent(Event event) {
        getCultureCenter().getEvents().add(event);
    }

    void update() {
        cultureCenter.updateRequests(populationCenter.getPopulation() / fertility + 1);
        populationCenter.executeRequests(getCultureCenter().getRequests(), territoryCenter.getTerritory());
        getPopulationCenter().strataUpdate(this);
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
            List<Tile> tiles = getOverallTerritory().getOuterBrink(t -> territoryCenter.canSettle(
                    t,
                    //TODO dont like territory checks in Group
                    t2 -> t2.getTagPool().getByType("Group").isEmpty() && parentGroup.getClosestInnerGroupDistance(t2) > 2
            ));
            if (tiles.isEmpty()) {
                return null;
            }
            if (!session.groupMultiplication) {
                return null;
            }
            Tile tile = randomElement(tiles, session.random);
            List<Aspect> aspects = cultureCenter.getAspectCenter().getAspectPool().getAll().stream()
                    .map(a -> a.copy(a.getDependencies()))
                    .collect(Collectors.toList());
            GroupMemes memes = new GroupMemes();
            memes.addAll(cultureCenter.getMemePool());
            return new Group(
                    parentGroup,
                    parentGroup.name + "_" + parentGroup.subgroups.size(),
                    populationCenter.getPart(0.5),
                    new RelationCenter(relationCenter.getHostilityCalculator$CulturesSimulation()),
                    tile,
                    aspects,
                    memes,
                    territoryCenter.getSpreadAbility()
            );
        }
        return null;
    }

    void intergroupUpdate() {
        if (session.isTime(session.groupTurnsBetweenBorderCheck)) {
            relationCenter.update(
                    getOverallTerritory()
                            .getOuterBrink()//TODO dont like territory checks in Group
                            .stream()
                            .flatMap(t -> t.getTagPool().getByType("Group").stream())
                            .map(t -> ((GroupTileTag) t).getGroup())
                            .distinct()
                            .filter(g -> g.parentGroup != parentGroup)
                            .collect(Collectors.toList()),
                    this
            );
        }
        cultureCenter.intergroupUpdate();
    }

    public void finishUpdate() {
        populationCenter.manageNewAspects(getCultureCenter().finishAspectUpdate());
        populationCenter.finishUpdate(cultureCenter.getMemePool());
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
            queue.addAll(cur.getTerritoryCenter().getAllNearGroups(cur).stream()
                    .filter(g -> g.parentGroup == parentGroup)
                    .filter(g -> !cluster.contains(g))
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
            group.parentGroup.removeGroup(group);
            conglomerate.addGroup(group);
        }
        session.world.addGroupConglomerate(conglomerate);
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
        for (Relation relation : relationCenter.getRelations()) {
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
