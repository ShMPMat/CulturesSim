package simulation;

import extra.ProbFunc;
import simulation.culture.Event;
import simulation.culture.aspect.Aspect;
import simulation.culture.group.Group;
import simulation.culture.thinking.meaning.GroupMemes;
import simulation.culture.thinking.meaning.Meme;
import simulation.space.WorldMap;
import simulation.space.generator.RandomMapGenerator;
import simulation.space.resource.Material;
import simulation.space.resource.Property;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourceIdeal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class which stores all entities in the simulation.
 */
public class World {
    /**
     * List of all Groups in the world, which are not subgroups.
     */
    public List<Group> groups;
    /**
     * List of all Aspects in the world.
     */
    public List<Aspect> aspectPool;
    /**
     * List of all Resources in the world.
     */
    public List<ResourceIdeal> resourcePool;
    /**
     * Base MemePool for the World. Contains all standard Memes.
     */
    private GroupMemes memePool;
    /**
     * World map on which all simulated objects are placed.
     */
    public WorldMap map;
    /**
     * All events which are linked to the world as a whole.
     */
    public List<Event> events;
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
    private int turn = 0;

    /**
     * Base constructor.
     *
     * @param numberOfGroups    how many groups will be spawned in the world.
     * @param mapSize           number from which map size will be computed. Guarantied that one of dimensions
     *                          will be equal to this number.
     * @param numberOrResources how many random resources will be created.
     */
    World(int numberOfGroups, int mapSize, int numberOrResources) {
        events = new ArrayList<>();
        memePool = new GroupMemes();
        fillAspectPool();
        fillPropertiesPool();
        fillMaterialPool();
        RandomMapGenerator.createResources(this, numberOrResources);
        map = new WorldMap(mapSize, mapSize * 2, resourcePool, this);
        RandomMapGenerator.fill(map);

        groups = new ArrayList<>();
        for (int i = 0; i < numberOfGroups; i++) {
            groups.add(new Group("G" + i, this, 100 + ProbFunc.randomInt(100), 1));
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
        aspectPool = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("SupplementFiles/Aspects"))) {
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
                aspectPool.add(new Aspect(tags, new HashMap<>(), null));
            }
        } catch (Throwable t) {
            System.err.println(t.toString());
        }
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
        return turn + "";
    }

    /**
     * Returns Aspect by name.
     *
     * @param name name of the Aspect.
     * @return Aspect with this name. If there is no such Aspect in the aspectPool
     * returns null and prints a warning.
     */
    public Aspect getAspectFromPoolByName(String name) {
        for (Aspect aspect : aspectPool) {
            if (aspect.getName().equals(name)) {
                return aspect;
            }
        }
        System.err.println("Unrecognized Aspect request - " + name);
        return null;
    }

    /**
     * Returns Property by name.
     *
     * @param name name of the Property.
     * @return Property with this name. If there is no such Property in the propertyPool
     * returns null and prints a warning.
     */
    public Property getPropertyFromPoolByName(String name) {
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
     * @return Resource with this name. If there is no such Resource in the resourcePool
     * returns null and prints a warning.
     */
    public ResourceIdeal getResourceFromPoolByName(String name) {
        for (ResourceIdeal resource : resourcePool) {
            if (resource.getName().equals(name)) {
                return resource;
            }
        }
        System.err.println("Unrecognized Resource request - " + name);
        return null;
    }

    /**
     * Returns Meme by name.
     * @param name name of the Meme.
     * @return Meme with this name.
     */
    public Meme getMemeFromPoolByName(String name) {
        return memePool.getMemeByName(name).copy();
    }

    /**
     * Returns Material by name.
     *
     * @param name name of the Material.
     * @return Material with this name. If there is no such Material in the materialPool
     * returns null and prints a warning.
     */
    public Material getMaterialFromPoolByName(String name) {
        for (Material material : materialPool) {
            if (material.getName().equals(name)) {
                return material;
            }
        }
        System.err.println("Unrecognized Material request - " + name);
        return null;
    }

    /**
     * Add an event into this World events.
     *
     * @param event Event which will be added.
     */
    public void addEvent(Event event) {
        events.add(event);
    }

    /**
     * Increment number of turns.
     */
    public void incrementTurn() {
        turn++;
    }

    public void addResources(List<Resource> resources) {
        resources.stream().filter(resource -> getResourceFromPoolByName(resource.getName()) == null)
                .forEach(resource -> resources.add(new ResourceIdeal(resource)));//TODO to make ideals in CW resource
    }
}
