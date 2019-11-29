package simulation.space;

import extra.ShnyPair;
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

import static simulation.Controller.sessionController;

/**
 * Basic tile of map
 */
public class Tile {
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
    /**
     * Level of this Tile.
     */
    private int level;
    /**
     * Lowest level of this Tile which corresponds to the ground level.
     */
    private int secondLevel;
    /**
     * Temperature for this Tile.
     */
    private int temperature;
    /**
     * Wind which is present on this Tile.
     */
    private Wind wind;
    private Wind _newWind;
    /**
     * Link to the world in which this Tile is present.
     */

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
        wind = new Wind();
        updateTemperature();
        group = null;
        setType(Type.Normal, true);
        resources = new ArrayList<>();
        _delayedResources = new ArrayList<>();
    }

    public List<Resource> getResources() {
        return resources;
    }

    public List<Resource> getAccessibleResources() {
        List<Resource> _l = new ArrayList<>(getResources());
        getNeighbours().forEach(tile -> _l.addAll(tile.getResources()));
        return _l;
    }

    public Resource getResource(Resource resource) {
        int index = resources.indexOf(resource);
        return index == -1 ? resource.cleanCopy(0) : resources.get(index);
    }

    /**
     * @param predicate Predicate on which neighbour Tiles will bw tested.
     * @return List of neighbour Tiles which satisfy the Predicate.
     */
    public List<Tile> getNeighbours(Predicate<Tile> predicate) {
        List<Tile> goodTiles = new ArrayList<>();
        WorldMap map = sessionController.world.map;
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
        return goodTiles;
    }

    /**
     * @return All neighbour Tiles.
     */
    public List<Tile> getNeighbours() {
        return getNeighbours(t -> true);
    }

    public TectonicPlate getPlate() {
        return plate;
    }

    /**
     * @param tile Tile to which distance will be calculated.
     * @return distance to the Tile (it is not guarantied it will be euclidean).
     */
    public int getDistance(Tile tile) {
        return Math.abs(tile.x - x) + Math.abs(tile.y - y);
    }

    /**
     * @param tiles Collection of tiles from which closest to this Tile will be found.
     * @return Closest Tile from tiles.
     */
    public Tile getClosest(Collection<Tile> tiles) {
        return tiles.stream().min(Comparator.comparingInt(this::getDistance)).orElse(null);
    }

    /**
     * @param tiles Collection of tiles in which distance will be calculated.
     * @return Distance to the closest tile from tiles.
     */
    public int getClosestDistance(Collection<Tile> tiles) {
        return getClosest(tiles).getDistance(this);
    }

    public Type getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getLevelWithWater() {
        int index = resources.indexOf(sessionController.world.getResourceFromPoolByName("Water"));
        return getLevel() + (index == -1 ? 0 : resources.get(index).getAmount());
    }

    public int getSecondLevel() {
        return secondLevel;
    }

    public int getTemperature() {
        return temperature;
    }

    public Wind getWind() {
        return wind;
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
//        if (level != 0) {
//            return;
//        }
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
        if (level < sessionController.defaultWaterLevel) {
            type = Type.Water;
        }
    }

    /**
     * Adds Resource to the Resources available on this Tile.
     * @param resource Resource which will be added.
     */
    private void addResource(Resource resource) {
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

    /**
     * Removes resource from this Tile if it is present.
     * @param resource resource which will be deleted.
     */
    public void removeResource(Resource resource) {
        for (int i = 0; i < resources.size(); i++) {
            Resource res = resources.get(i);
            if (res.fullEquals(resource)) {
                resources.remove(i);
                return;
            }
        }
    }

    /**
     * Adds resources which will be available on this Tile on the next turn.
     * @param resource resource which will be added.
     */
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

    private void checkIce() {
        if (type == Type.Water && temperature < -10) {
            type = Type.Ice;
            level = sessionController.defaultWaterLevel;
        } else if (type == Type.Ice && temperature > 0) {
            type = Type.Water;
            level = secondLevel;
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

    /**
     * Starts overgroupUpdate for this Tile.
     */
    public void startUpdate() {
        _newWind = new Wind();
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
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);//TODO smarter wind, now it can blow away everything on the first tile by order.
            if (!resource.isMovable()) {
                continue;
            }
            for (ShnyPair<Tile, Double> pair : wind.affectedTiles) {
                int part = (int) (resource.getAmount() * Math.min(pair.second * 0.0001 / resource.getGenome().getMass(), 1));
                if (part > 0) {
                    pair.first.addDelayedResource(resource.getCleanPart(part));
                }
            }
        }
        updateTemperature();
        if (getType() == Type.Normal || getType() == Type.Woods || getType() == Type.Growth) {
            if (resources.stream().anyMatch(resource -> resource.getSimpleName().equals("Tree"))) {
                setType(Type.Woods, false);
            } else if (resources.stream().anyMatch(resource -> resource.getGenome().getType() == Genome.Type.Plant)) {
                setType(Type.Growth, false);
            } else {
                setType(Type.Normal, false);
            }
        } else if (getType() == Type.Water) {
            addDelayedResource(sessionController.world.getResourceFromPoolByName("Vapour").copy(50));
        }
        if (resources.contains(sessionController.world.getResourceFromPoolByName("Water"))) {
            addDelayedResource(sessionController.world.getResourceFromPoolByName("Vapour").copy(50));
        }
    }

    public void middleUpdate() {
        WorldMap map = sessionController.world.map;
        setWindByTemperature(map.get(x + 1, y));
        setWindByTemperature(map.get(x - 1, y));
        setWindByTemperature(map.get(x, y + 1));
        setWindByTemperature(map.get(x, y - 1));

        if (!_newWind.isStill()) {
            return;
        }
        propagateWindStraight(map.get(x - 1, y), map.get(x + 1, y));
        propagateWindStraight(map.get(x + 1, y), map.get(x - 1, y));
        propagateWindStraight(map.get(x, y - 1), map.get(x, y + 1));
        propagateWindStraight(map.get(x, y + 1), map.get(x, y - 1));

        if (!_newWind.isStill()) {//TODO better to add wind for cross tiles than try to fetch it; cut wind on large level changes
            return;
        }
//        propagateWindWithCondition(map.get(x - 1, y), map.get(x + 1, y - 1), map.get(x, y - 1));
//        propagateWindWithCondition(map.get(x - 1, y), map.get(x + 1, y + 1), map.get(x, y + 1));
//
//        propagateWindWithCondition(map.get(x + 1, y), map.get(x - 1, y - 1), map.get(x, y - 1));
//        propagateWindWithCondition(map.get(x + 1, y), map.get(x - 1, y + 1), map.get(x, y + 1));
//
//        propagateWindWithCondition(map.get(x, y + 1), map.get(x - 1, y - 1), map.get(x - 1, y));
//        propagateWindWithCondition(map.get(x, y + 1), map.get(x + 1, y - 1), map.get(x + 1, y));
//
//        propagateWindWithCondition(map.get(x, y - 1), map.get(x - 1, y + 1), map.get(x - 1, y));
//        propagateWindWithCondition(map.get(x, y - 1), map.get(x + 1, y + 1), map.get(x + 1, y));
    }

    private void setWindByTemperature(Tile tile) {
        int temperatureChange = 3;
        if (tile != null) {
            double level = ((double) tile.temperature - 1 - temperature) / temperatureChange;
            if (level > 0) {
                _newWind.changeLevelOnTile(tile, level);
            }
        }
    }

    private void propagateWindStraight(Tile target, Tile tile) {
        if (tile != null && target != null) {
            double level = tile.wind.getLevelByTile(this) - sessionController.windPropagation;
            if (level > 0) {
                _newWind.changeLevelOnTile(target, level);
            }
        }
    }

    private void propagateWindWithCondition(Tile target, Tile tile, Tile wanted) {
        if (tile != null && target != null && wanted != null) {
            double level = tile.wind.getLevelByTile(wanted) - sessionController.windPropagation * 5;
            if (level > 0) {
                _newWind.changeLevelOnTile(target, level);
            }
        }
    }

    public void finishUpdate() {
        wind = _newWind;
    }

    private void updateTemperature() {
        temperature = x + sessionController.temperatureBaseStart -
                Math.max(0, (level - 110) / 2) - (type == Type.Water || type == Type.Ice ? 10 : 0);
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
        StringBuilder stringBuilder = new StringBuilder("Tile " + x + " " + y +
                (group == null ? "" : ", group=" + group.name) + ", type=" + getType() + ", temperature=" + temperature
                + ", level=" + level + ", resource:\n");
        for (Resource resource : resources) {
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
