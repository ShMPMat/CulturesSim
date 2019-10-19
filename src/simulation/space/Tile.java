package simulation.space;

import simulation.World;
import simulation.culture.group.Group;
import simulation.space.resource.Genome;
import simulation.space.resource.Resource;

import java.util.ArrayList;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Basic tile of map
 */
public class Tile { //TODO woods type
    public enum Type {
        Normal,
        Mountain,
        Water,
        Ice,
        Woods
    }

    /**
     * Coordinates of this Tile on the WorldMap.
     */
    public int x, y;
    /**
     * Group which is settled on this tile.
     */
    public Group group;
    /**
     * Type of this Tile.
     */
    private Type type;

    /**
     * TectonicPlate by which this Tile is owned.
     */
    private TectonicPlate plate;
    /**
     * Resources which are located on this Tile.
     */
    private List<Resource> resources;
    /**
     * Resources which were added on this Tile during this turn. They are
     * stored here before the end of the turn.
     */
    private List<Resource> _delayedResources;
    private int level;
    private int temperature;
    private World world;

    public Tile(int x, int y, World world) {
        this.x = x;
        this.y = y;
        temperature = x - 5;
        this.world = world;
        group = null;
        setType(Type.Normal);
        resources = new ArrayList<>();
        _delayedResources = new ArrayList<>();
    }

    public List<Resource> getResources() {
        return resources;
    }

    public World getWorld() {
        return world;
    }

    public List<Tile> getNeighbours(Predicate<Tile> predicate) {
        List<Tile> goodTiles = new ArrayList<>();
        WorldMap map = getWorld().map;
        Tile newTile = map.get(x, y + 1);
        if (newTile != null && predicate.test(newTile)) {
            goodTiles.add(newTile);
        }
        newTile = map.get(x, y - 1);
        if (newTile != null && predicate.test(newTile)) {
            goodTiles.add(newTile);
        }
        newTile = map.get(x + 1, y);
        if (newTile != null && predicate.test(newTile)) {
            goodTiles.add(newTile);
        }
        newTile = map.get(x - 1, y);
        if (newTile != null && predicate.test(newTile)) {
            goodTiles.add(newTile);
        }
//        newTile = map.get(x + 1, y + 1);
//        if (newTile != null && predicate.test(newTile)) {
//            goodTiles.add(newTile);
//        }
//        newTile = map.get(x - 1, y + 1);
//        if (newTile != null && predicate.test(newTile)) {
//            goodTiles.add(newTile);
//        }
//        newTile = map.get(x + 1, y - 1);
//        if (newTile != null && predicate.test(newTile)) {
//            goodTiles.add(newTile);
//        }
//        newTile = map.get(x - 1, y - 1);
//        if (newTile != null && predicate.test(newTile)) {
//            goodTiles.add(newTile);
//        }

        return goodTiles;
    }

    public TectonicPlate getPlate() {
        return plate;
    }

    public int getDistance(Tile tile) {
        return Math.abs(tile.x - x) + Math.abs(tile.y - y);
    }

    public Tile getClosest(Collection<Tile> tiles) {
        return tiles.stream().min(Comparator.comparingInt(this::getDistance)).orElse(null);
    }

    public int getClosestDistance(Collection<Tile> tiles) {
        return getClosest(tiles).getDistance(this);
    }

    public Type getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getTemperature() {
        return temperature;
    }

    public void addResource(Resource resource) {
        if (resource.getAmount() == 0) {
            return;
        }
        if (!resource.isMovable() && resource.getTile() != null) {
            return;
        }
        for (Resource res : resources) {
            if (res.fullEquals(resource)) {
                res.merge(resource);
                return;
            }
        }
        resources.add(resource);
        resource.setTile(this);
    }

    public void removeResource(Resource resource) {
        for (int i = 0; i < resources.size(); i++) {
            Resource res = resources.get(i);
            if (res.fullEquals(resource)) {
                resources.remove(i);
                return;
            }
        }
    }

    public void addDelayedResource(Resource resource) {
        if (resource.getAmount() == 0) {
            return;
        }
//        if (resource.isMovable()) {
//            System.err.println("Movable resource added in Delayed resources.");
//            return;
//        }
        _delayedResources.add(resource);
        resource.setTile(this);
    }

    public void setPlate(TectonicPlate plate) {
        this.plate = plate;
    }

    public void setType(Type type) {
        this.type = type;
        switch (type) {
            case Mountain:
                level = 110;
                break;
            case Normal:
                level = 100;
                break;
            case Water:
                level = 85;
                break;
        }
    }

    public void setLevel(int level) {
        this.level = level;
        type = Type.Normal;
        if (level >= 110) {
            type = Type.Mountain;
        }
        if (level < world.getWaterLevel()) {
            type = Type.Water;
        }
    }

    private void checkIce() { //TODO defreezing
        if (type == Type.Water && temperature < 0) {
            type = Type.Ice;
            level = world.getWaterLevel();
        }
    }

    public int closestTileWithResources(Collection<Resource> requirements) {
//        List<Tile> tiles = new ArrayList<>(), layer;
//        tiles.add(this);
//        if (resource.containsAll(requirements)) {
//            return 0;
//        }
//        int i = 1;
//        while (true) {
//            layer = new Territory(tiles).getBrinkWithCondition(tile -> true);
//            if (layer.size() >= 100) { //TODO INFINITE TILES BUG
//                return -1;
//            }
//            if (layer.size() == 0) {
//                return -1;
//            }
//            List _l = layer.stream().filter(tile -> tile.resource.containsAll(requirements))
//                    .collect(Collectors.toList());
//            if (_l.size() > 0) {
//                return i;
//            }
//            tiles.addAll(layer);
//            i++;
//        }
        if (resources.stream().anyMatch(requirements::contains)) {
            return 0;
        }
        return 1;
    }

    public boolean canSettle(Group group) {
        return getType() != Type.Water && getType() != Type.Mountain ||
                (getType() == Type.Mountain && group.getAspects().stream().anyMatch(aspect -> aspect.getTags().stream()
                        .anyMatch(aspectTag -> aspectTag.name.equals("mountainLiving"))));
    }

    public boolean canSettle(Genome genome) {
        return genome.isTypeAcceptable(getType());
    }

    public void update() {
        checkIce();
        _delayedResources = _delayedResources.stream().filter(resource -> this.equals(resource.getTile())).collect(Collectors.toList());
        _delayedResources.forEach(resource -> resource.setTile(null));
        _delayedResources.forEach(this::addResource);
        _delayedResources.clear();
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            if (!resource.update()) {
                resources.remove(resource);
                i--;
            }
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
        StringBuilder stringBuilder = new StringBuilder("Tile " + x + " " + y +
                (group == null ? "" : ", group=" + group.name) + ", type=" + getType() + ", temperature=" + temperature
                + ", level=" + level + ", resource:\n");
        for (Resource resource : resources) {
            stringBuilder.append(resource).append("\n");
        }
        return stringBuilder.toString();
    }
}
