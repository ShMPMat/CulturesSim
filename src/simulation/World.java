package simulation;

import extra.SpaceProbabilityFuncs;
import kotlin.ranges.IntRange;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectInstantiation;
import simulation.culture.aspect.AspectPool;
import simulation.culture.group.GroupConglomerate;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.*;
import simulation.space.Tile;
import simulation.space.WorldMap;
import simulation.space.generator.MapGeneratorSupplement;
import simulation.space.generator.MapGeneratorKt;
import simulation.space.resource.material.MaterialInstantiation;
import simulation.space.resource.material.MaterialPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static simulation.Controller.*;
import static simulation.space.generator.MapGeneratorKt.generateMap;

/**
 * Class which stores all entities in the simulation.
 */
public class World {
    public List<GroupConglomerate> groups = new ArrayList<>();
    private AspectPool aspectPool = (new AspectInstantiation())
            .createPool("SupplementFiles/Aspects");
    private MaterialPool materialPool = (new MaterialInstantiation(aspectPool))
            .createPool("SupplementFiles/Materials");
    private ResourcePool resourcePool;
    /**
     * Base MemePool for the World. Contains all standard Memes.
     */
    private GroupMemes memePool = new GroupMemes();
    public WorldMap map;
    /**
     * All events which are linked to the world as a whole.
     */
    public List<Event> events = new ArrayList<>();
    /**
     * How many turns passed from the beginning of the simulation.
     */
    private int turn = 0, thousandTurns = 0, millionTurns = 0;

    World() {
        resourcePool = (new ResourceInstantiation(
                "SupplementFiles/Resources",
                aspectPool,
                materialPool
        )).createPool();
        map = generateMap(session.mapSizeX, session.mapSizeY, session.platesAmount, session.random);
    }

    public void fillResources() {
        MapGeneratorKt.fillResources(
                map,
                resourcePool,
                new MapGeneratorSupplement(
                        new IntRange(session.startResourceAmountMin, session.startResourceAmountMax)
                ),
                session.random);
    }

    public void initializeFirst() {
        for (int i = 0; i < session.startGroupAmount; i++) {
            groups.add(new GroupConglomerate(1, getTileForGroup()));
        }
        groups.forEach(group ->
                group.subgroups.forEach(s -> s.getCulturalCenter().addAspect(aspectPool.get("TakeApart")))
        );
        groups.forEach(group ->
                group.subgroups.forEach(s -> s.getCulturalCenter().addAspect(aspectPool.get("Take")))
        );
        groups.forEach(GroupConglomerate::finishUpdate);
    }

    private Tile getTileForGroup() {
        while (true) {
            Tile tile = SpaceProbabilityFuncs.randomTile(session.world.map);
            if (tile.group == null && tile.canSettle()) {
                return tile;
            }
        }
    }

    /**
     * Getter for turn.
     *
     * @return how many turns passed since the beginning of the simulation.
     */
    public String getTurn() {
        return turn + thousandTurns * 1000 + millionTurns * 1000000 + "";
    }

    public int getLesserTurnNumber() {
        return turn;
    }

    public AspectPool getAspectPool() {
        return aspectPool;
    }

    public MaterialPool getMaterialPool() {
        return materialPool;
    }

    public ResourcePool getResourcePool() {
        return resourcePool;
    }

    /**
     * Returns Meme by name.
     *
     * @param name name of the Meme.
     * @return Meme with this name.
     */
    public Meme getPoolMeme(String name) {
        try {
            return memePool.getMemeCopy(name);
        } catch (Exception e) {
            int i = 0;
        }
        return null;
    }

    public Collection<Aspect> getAllDefaultAspects() {
        return aspectPool.getAll();
    }

    /**
     * Add an event into this World events.
     *
     * @param event Event which will be added.
     */
    public void addEvent(Event event) {
        events.add(event);
    }

    public void addGroup(GroupConglomerate group) {
        groups.add(group);
    }

    /**
     * Increment number of turns.
     */
    public void incrementTurn() {
        turn++;
        if (turn == 1000) {
            turn = 0;
            incrementTurnEvolution();
        }
    }

    public void incrementTurnEvolution() {
        thousandTurns++;
        if (thousandTurns == 1000) {
            thousandTurns = 0;
            incrementTurnGeology();
        }
    }

    public void incrementTurnGeology() {
        millionTurns++;
    }

    @Override
    public String toString() {
        return getTurn();
    }
}
