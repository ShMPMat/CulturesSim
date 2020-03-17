package simulation.space.tile;

import simulation.space.*;
import simulation.space.resource.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static simulation.Controller.session;

public class Tile {
    public int x, y;
    private MutableTileTagPool tagPool = new MutableTileTagPool();
    private Type type;
    private TectonicPlate plate;
    private MutableResourcePack resourcePack = new MutableResourcePack();
    /**
     * Resources which were added on this Tile during this turn. They are
     * stored here before the end of the turn.
     */
    private List<Resource> _delayedResources = new ArrayList<>();
    /**
     * Level of this Tile.
     */
    private int level;
    /**
     * Lowest level of this Tile which corresponds to the ground level.
     */
    private int secondLevel;
    private int temperature;
    private List<Tile> neighbours = null;
    private WindCenter windCenter = new WindCenter();

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
        updateTemperature();
        setType(Type.Normal, true);
    }

    public void setNeighbours(List<Tile> neighbours) {
        if (this.neighbours != null) {
            throw new SpaceError("Neighbours are already set");
        }
        this.neighbours = neighbours;
    }

    public ResourcePack getResourcePack() {
        return resourcePack;
    }

    public MutableTileTagPool getTagPool() {
        return tagPool;
    }

    /**
     * @return all Resources on this Tile including ones which were
     * moved on this Tile on this turn and inaccessible outside of Tile.
     */
    public List<Resource> getResourcesWithMoved() {
        List<Resource> _l = new ArrayList<>(resourcePack.getResources());
        _l.addAll(_delayedResources);
        return _l;
    }

    /**
     * @return all Resources which are available from this Tile.
     */
    public List<Resource> getAccessibleResources() {
        List<Resource> accessibleResources = new ArrayList<>(resourcePack.getResources());
        getNeighbours().forEach(t -> accessibleResources.addAll(t.resourcePack.getResources()));
        return accessibleResources;
    }

    public Resource getResource(String name) {//TODO awful
        Resource resource = session.world.getResourcePool().get(name);
        return resourcePack.getResource(resource);
    }

    public List<Tile> getNeighbours(Predicate<Tile> predicate) {
        return neighbours.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public List<Tile> getNeighbours() {
        return neighbours;
    }

    public TectonicPlate getPlate() {
        return plate;
    }

    public Type getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getSecondLevel() {
        return secondLevel;
    }

    public int getTemperature() {
        return temperature;
    }

    public Wind getWind() {
        return windCenter.getWind();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPlate(TectonicPlate plate) {
        this.plate = plate;
    }

    public void setType(Type type, boolean updateLevel) {
        if (type == this.type) {
            return;
        }
        this.type = type;
        if (!updateLevel) {
            return;
        }
        switch (type) {
            case Mountain:
                level = 110;
                secondLevel = 110;
                break;
            case Normal:
                level = 100;
                secondLevel = 100;
                break;
            case Water:
                level = 85;
                secondLevel = 85;
                break;
        }
    }

    public void setLevel(int level) {
        this.level = level;
        this.secondLevel = level;
        type = Type.Normal;
        if (level >= 110) {
            type = Type.Mountain;
        }
        if (level < session.defaultWaterLevel) {
            type = Type.Water;
        }
    }

    private void addResource(Resource resource) {
        if (resource.getAmount() == 0) {
            return;
        }
        resourcePack.add(resource);
    }

    /**
     * Adds resources which will be available on this Tile on the next turn.
     *
     * @param resource resource which will be added.
     */
    public void addDelayedResource(Resource resource) {
        if (resource.getAmount() == 0) {
            return;
        }
        _delayedResources.add(resource);
    }

    private void checkIce() {
        if (type == Type.Water && temperature < -10) {
            type = Type.Ice;
            level = session.defaultWaterLevel;
        } else if (type == Type.Ice && temperature > 0) {
            type = Type.Water;
            level = secondLevel;
        }
    }

    public int hasResources(Collection<Resource> requirements) {//TODO remove
        return resourcePack.getResources().stream()
                .filter(requirements::contains)
                .map(Resource::getAmount)
                .reduce(0, Integer::sum);
    }

    public boolean canSettle() {
        return getType() != Type.Water && getType() != Type.Mountain;
    }//TODO move it otta here

    public void startUpdate() { //TODO wind blows on 2 neighbour tiles
        updateResources();
        windCenter.startUpdate();
        windCenter.useWind(resourcePack.getResources());
        updateTemperature();
        updateType();
        checkIce();
    }

    public void middleUpdate() {
        _delayedResources.forEach(this::addResource);
        _delayedResources.clear();
        windCenter.middleUpdate(x, y);
    }

    public void finishUpdate() {
        windCenter.finishUpdate();
    }

    private void updateTemperature() {
        temperature = session.temperatureBaseStart +
                x * (session.temperatureBaseFinish - session.temperatureBaseStart) / session.mapSizeX -
                Math.max(0, (level - 110) / 2) -
                (type == Type.Water || type == Type.Ice ? 10 : 0);
    }

    private void updateResources() {
        List<Resource> deletedResources = new ArrayList<>();
        for (Resource resource: resourcePack.getResources()) {
            if (!resource.update(this)) {
                deletedResources.add(resource);
            }
        }
        resourcePack.removeAll(deletedResources);
    }

    private void updateType() {
        if (resourcePack.contains(session.world.getResourcePool().get("Water"))) {
            setType(Type.Water, false);
        }
        if (getType() == Type.Normal || getType() == Type.Woods || getType() == Type.Growth) {
            if (resourcePack.getResources().stream().anyMatch(r -> r.getSimpleName().matches("Tree|JungleTree"))) {
                setType(Type.Woods, false);
            } else if (resourcePack.getResources().stream().anyMatch(r -> r.getGenome().getType() == Genome.Type.Plant)) {
                setType(Type.Growth, false);
            } else {
                setType(Type.Normal, false);
            }
        } else if (getType() == Type.Water) {
            addDelayedResource(session.world.getResourcePool().get("Vapour").copy(50));
        }
        if (resourcePack.contains(session.world.getResourcePool().get("Water"))) {
            addDelayedResource(session.world.getResourcePool().get("Vapour").copy(50));
        }
    }

    public void levelUpdate() {//TODO works bad on Ice; wind should affect mountains mb they will stop grow
        for (int i = 0; i < (type == Type.Water ? 4 : (level > 105 && level < 120 ? 5 : 1)); i++) {
            distributeLevel();
        }
    }

    private void distributeLevel() {
        List<Tile> tiles = getNeighbours();
        tiles.sort(Comparator.comparingInt(tile -> tile.secondLevel));
        Tile lowest = tiles.get(0);
        if (level == 110 && lowest.level == 100) {
            int i = 0;
        }
        if (lowest.secondLevel + 1 < secondLevel) {
            setLevel(level - 1);
            lowest.setLevel(lowest.level + 1);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return x == tile.x && y == tile.y;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(String.format(
                "Tile %d %d, type=%s, temperature=%d, level=%d, resources:\n", x, y, getType(), temperature, level
                ));
        for (Resource resource : resourcePack.getResources()) {
            stringBuilder.append(resource).append("\n");
        }
        return stringBuilder.toString();
    }

    public enum Type {
        Normal,
        Mountain,
        Water,
        Ice,
        Woods,
        Growth
    }
}
