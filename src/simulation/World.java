package simulation;

import extra.InputDatabase;
import extra.SpaceProbabilityFuncs;
import kotlin.ranges.IntRange;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectPool;
import simulation.culture.group.GroupConglomerate;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.resource.*;
import simulation.space.Tile;
import simulation.space.WorldMap;
import simulation.space.generator.MapGeneratorSupplement;
import simulation.space.generator.MapGeneratorKt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static simulation.Controller.*;
import static simulation.space.generator.MapGeneratorKt.generateMap;

/**
 * Class which stores all entities in the simulation.
 */
public class World {
    public List<GroupConglomerate> groups = new ArrayList<>();
    private AspectPool aspectPool;
    /**
     * List of all Resources in the world.
     */
    private ResourcePool resourcePool;
    /**
     * Base MemePool for the World. Contains all standard Memes.
     */
    private GroupMemes memePool = new GroupMemes();
    /**
     * World map on which all simulated objects are placed.
     */
    public WorldMap map;
    /**
     * All events which are linked to the world as a whole.
     */
    public List<Event> events = new ArrayList<>();
    /**
     * List of all Materials in the world.
     */
    private List<Material> materialPool;
    /**
     * List of all Properties in the world.
     */
    private List<Property> propertyPool;
    /**
     * How many turns passed from the beginning of the simulation.
     */
    private int turn = 0, thousandTurns = 0, millionTurns = 0;

    World() {
        fillAspectPool();
        fillPropertiesPool();
        fillMaterialPool();
    }

    void initializeZero() {
        resourcePool = ResourceInstantiatorKt.createPool("SupplementFiles/Resources", aspectPool);
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
     * Reads all Properties from supplement file and fills propertyPool with them.
     */
    private void fillPropertiesPool() {
        propertyPool = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("SupplementFiles/Properties"))) {
            String line;
            String[] tags;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (isLineBad(line)) {
                    continue;
                }
                tags = line.split("\\s+");
                propertyPool.add(new Property(tags, this));
            }
        } catch (Throwable t) {
            System.err.println(t.toString());
        }
    }

    /**
     * Reads all Materials from supplement file and fills materialPool with them.
     */
    private void fillMaterialPool() {
        materialPool = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("SupplementFiles/Materials"))) {
            String line;
            String[] tags;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (isLineBad(line)) {
                    continue;
                }
                tags = line.split("\\s+");
                materialPool.add(new Material(tags, this));
            }
        } catch (Throwable t) {
            System.err.println(t.toString());
        }
        materialPool.forEach(Material::actualizeLinks);
    }

    /**
     * Reads all Aspects from supplement file and fills aspectPool with them.
     */
    private void fillAspectPool() {
        List<Aspect> aspects = new ArrayList<>();
        InputDatabase inputDatabase = new InputDatabase("SupplementFiles/Aspects");
        String line;
        String[] tags;
        while (true) {
            line = inputDatabase.readLine();
            if (line == null) {
                break;
            }
            tags = line.split("\\s+");
            aspects.add(new Aspect(tags, new HashMap<>(), null));
        }
        aspectPool = new AspectPool(aspects);
    }

    private boolean isLineBad(String line) {
        return line.trim().isEmpty() || line.charAt(0) == '/';
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

    /**
     * Returns Property by name.
     *
     * @param name name of the Property.
     * @return Property with this name. If there is no such Property in the propertyPool
     * returns null and prints a warning.
     */
    public Property getPoolProperty(String name) {
        for (Property property : propertyPool) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        System.err.println("Unrecognized Property request - " + name);
        return null;
    }

    /**
     * Returns Resource by name.
     *
     * @param name name of the Resource.
     * @return Resource with this sentenceBase name. If there is no such Resource in the resourcePool
     * returns null and prints a warning.
     */
    public Resource getPoolResource(String name) {
        return resourcePool.get(name);
    }

    public List<Resource> getAllResources() {
        return resourcePool.getWithPredicate(t -> true);
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

    /**
     * Returns Material by name.
     *
     * @param name name of the Material.
     * @return Material with this name. If there is no such Material in the materialPool
     * returns null and prints a warning.
     */
    public Material getPoolMaterial(String name) {
        for (Material material : materialPool) {
            if (material.getName().equals(name)) {
                return material;
            }
        }
        System.err.println("Unrecognized Material request - " + name);
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
