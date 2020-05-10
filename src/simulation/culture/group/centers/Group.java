package simulation.culture.group.centers;

import extra.OutputFunc;
import kotlin.Pair;
import simulation.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.group.*;
import simulation.culture.group.cultureaspect.CultureAspect;
import simulation.culture.group.request.Request;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.MemeSubject;
import simulation.space.Territory;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.tile.Tile;

import java.util.*;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.randomElement;
import static shmp.random.RandomProbabilitiesKt.testProbability;
import static simulation.Controller.session;

public class Group {
    public State state = State.Live;
    public String name;

    private int fertility = session.defaultGroupFertility;
    private GroupConglomerate parentGroup;
    private CultureCenter cultureCenter;
    private TerritoryCenter territoryCenter;
    private PopulationCenter populationCenter;
    private RelationCenter relationCenter;
    private ResourceCenter resourceCenter;

    private int _direNeedTurns = 0;

    public Group(
            ResourceCenter resourceCenter,
            GroupConglomerate parentGroup,
            String name,
            PopulationCenter populationCenter,
            RelationCenter relationCenter,
            Tile tile,
            List<Aspect> aspects,
            GroupMemes memePool,
            Collection<CultureAspect> cultureAspects,
            double spreadAbility
    ) {
        this.name = name;
        this.parentGroup = parentGroup;
        this.resourceCenter = resourceCenter;
        this.populationCenter = populationCenter;
        cultureCenter = new CultureCenter(this, memePool, aspects);
        territoryCenter = new TerritoryCenter(this, spreadAbility, tile);
        this.relationCenter = relationCenter;
        copyCA(cultureAspects);
    }

    private void copyCA(Collection<CultureAspect> aspects) {
        List<CultureAspect> retry = new ArrayList<>();
        for (CultureAspect aspect : aspects) {
            CultureAspect copy = aspect.adopt(this);
            if (copy == null) {
                retry.add(aspect);
            }
            cultureCenter.getCultureAspectCenter().addCultureAspect(copy);
        }
        if (!retry.isEmpty()) {
            if (retry.size() == aspects.size()) {
                throw new GroupError("Cannot adopt CultureAspect " + retry.get(0));
            }
            copyCA(retry);
        }
    }

    public ResourceCenter getResourceCenter() {
        return resourceCenter;
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

    public void setParentGroup(GroupConglomerate parentGroup) {
        this.parentGroup = parentGroup;
    }

    void die() {
        state = State.Dead;
        resourceCenter.die();
        populationCenter.die();
        territoryCenter.die();
        cultureCenter.die();
        addEvent(new Event(Event.Type.Death, "Group " + name + " died", "group", this));
        for (Group group : relationCenter.getRelatedGroups()) {
            group.cultureCenter.getMemePool().addMemeCombination(
                    cultureCenter.getMemePool().getMeme("group")
                            .addPredicate(new MemeSubject(name))
                            .addPredicate(cultureCenter.getMemePool().getMeme("die"))
            );
        }
    }

    public void addEvent(Event event) {
        getCultureCenter().getEvents().add(event);
    }

    public void update() {
        long others = System.nanoTime();
        populationCenter.update(territoryCenter.getAccessibleTerritory(), this);
        cultureCenter.updateRequests(populationCenter.getPopulation() / fertility + 1);
        populationCenter.executeRequests(cultureCenter.getTurnRequests());
        territoryCenter.update();
        if (state == State.Dead) {
            return;
        }
        long main = System.nanoTime();
        if (shouldMigrate()) {
            if (territoryCenter.migrate()) {
                resourceCenter.moveToNewStorage(territoryCenter.getTerritory().getCenter());
            }
        }
        if (populationCenter.isMinPassed(territoryCenter.getTerritory())) {
            territoryCenter.expand();
        } else {
            territoryCenter.shrink();
        }
        session.groupMigrationTime += System.nanoTime() - main;
        checkNeeds();
        cultureCenter.update();
        session.groupInnerOtherTime += System.nanoTime() - others;
    }

    private void checkNeeds() {
        Pair<ResourceLabeler, ResourceNeed> need = resourceCenter.getDireNeed();
        if (need == null) {
            return;
        }
        cultureCenter.addNeedAspect(need);
        populationCenter.wakeNeedStrata(need);

    }

    private boolean shouldMigrate() {
        if (testProbability(0.9, session.random)) {//TODO worth it?
            return false;
        }
        if (resourceCenter.hasDireNeed()) {
            _direNeedTurns++;
        } else {
            _direNeedTurns = 0;
        }
        return _direNeedTurns > 5 + territoryCenter.getNotMoved() / 10;
    }

    public ConglomerateCommand populationUpdate() {
        if (populationCenter.getPopulation() == 0) {
            die();
            return null;
        }
        int denominator = parentGroup.subgroups.size() + 1;
        if ((populationCenter.isMaxReached(territoryCenter.getTerritory())
                || testProbability(session.defaultGroupDiverge / denominator, session.random))) {
            List<Tile> tiles = getOverallTerritory().getOuterBrink(t ->
                            territoryCenter.canSettleAndNoGroup(t) && parentGroup.getClosestInnerGroupDistance(t) > 2
                    //TODO dont like territory checks in Group
            );
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
            MutableResourcePack pack = new MutableResourcePack();
            resourceCenter.getPack().getResources().forEach(r ->
                    pack.addAll(resourceCenter.takeResource(r, r.getAmount() / 2))
            );
            String name = parentGroup.getNewName();
            return new Add(new Group(
                    new ResourceCenter(pack, tile, name),
                    parentGroup,
                    name,
                    populationCenter.getPart(0.5),
                    new RelationCenter(relationCenter.getHostilityCalculator$CulturesSimulation()),
                    tile,
                    aspects,
                    memes,
                    cultureCenter.getCultureAspectCenter().getAspectPool().getAll(),
                    territoryCenter.getSpreadAbility()
            ));
        }
        return null;
    }

    public void intergroupUpdate() {
        relationCenter.requestTrade(cultureCenter.getTurnRequests());
        if (session.isTime(session.groupTurnsBetweenBorderCheck)) {
            List<Group> toUpdate = getOverallTerritory()
                    .getOuterBrink()//TODO dont like territory checks in Group
                    .stream()
                    .flatMap(t -> t.getTagPool().getByType("Group").stream())
                    .map(t -> ((GroupTileTag) t).getGroup())
                    .collect(Collectors.toList());
            toUpdate.addAll(parentGroup.subgroups);
            toUpdate = toUpdate.stream().distinct().collect(Collectors.toList());
            toUpdate.remove(this);
            relationCenter.updateRelations(toUpdate, this);
        }
        cultureCenter.intergroupUpdate();
    }

    public void finishUpdate() {
        resourceCenter.addAll(cultureCenter.getTurnRequests().finish());
        populationCenter.manageNewAspects(getCultureCenter().finishAspectUpdate());
        populationCenter.finishUpdate(this);
        resourceCenter.finishUpdate();
    }

    public boolean diverge() {
        if (!session.groupDiverge) {
            return false;
        }
        if (parentGroup.subgroups.size() <= 1) {
            return false;
        }
        double relations = relationCenter.getAvgConglomerateRelation(parentGroup);
        double exitProbability = session.defaultGroupExiting /*/ (relations * relations * relations)*/;
        if (testProbability(exitProbability, session.random)) {
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

    public ResourcePack askFor(Request request, Group owner) {
        if (!owner.parentGroup.equals(parentGroup)) return new ResourcePack();
        return populationCenter.executeRequest(request.reassign(this)).getPack();
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
        StringBuilder builder = new StringBuilder("Group " + name + " is " + state + ", population="
                + populationCenter.getPopulation() + ", aspects:\n");
        for (Aspect aspect : cultureCenter.getAspectCenter().getAspectPool().getAll()) {
            builder.append(aspect).append("\n\n");
        }
        StringBuilder s = new StringBuilder();
        s.append("Aspects: ");
        for (CultureAspect aspect : cultureCenter.getCultureAspectCenter().getAspectPool().getAll()) {
            s.append(aspect).append(", ");
        }
        s.append((cultureCenter.getCultureAspectCenter().getAspectPool().isEmpty() ? "none\n" : "\n"));
        builder.append(s.toString());
        builder.append("\n").append(resourceCenter.toString())
                .append(populationCenter.toString())
                .append("\n").append(relationCenter)
                .append("\n");
        builder.append(cultureCenter.getTurnRequests());
        builder = OutputFunc.chompToSize(builder, 70);

        return builder.toString();
    }

    public enum State {
        Live,
        Dead
    }
}
