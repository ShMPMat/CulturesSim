package simulation.space.tile;

import kotlin.Pair;
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
    private Wind wind;
    private Wind _newWind;
    public boolean fixedWater = false;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
        wind = new Wind();
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

    public List<Tile> getNeighboursInRadius(Predicate<Tile> predicate, int radius) {
        List<Tile> tiles = new ArrayList<>();
        Queue<Tile> candidates = new ArrayDeque<>(getNeighbours());
        while (!candidates.isEmpty()) {
            Tile candidate = candidates.poll();
            if (TileDistanceKt.isCloser(this, candidate, radius) && !tiles.contains(candidate) && this != candidate) {
                tiles.add(candidate);
                candidates.addAll(candidate.getNeighbours());
            }
        }
        tiles.removeIf(predicate.negate());
        return tiles;
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

    public int getLevelWithWater() {
        Resource water = resourcePack.getResource(session.world.getResourcePool().get("Water"));
        return getLevel() + water.getAmount();
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
        _newWind = new Wind();
        checkIce();
        updateResources();
        useWind();
        updateTemperature();
        updateType();
    }

    public void middleUpdate() {
        _delayedResources.forEach(this::addResource);
        _delayedResources.clear();
        WorldMap map = session.world.map;
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

        if (!_newWind.isStill()) {//TODO better to addAll wind for cross tiles than try to fetch it; cut wind on large level changes
            return;
        }

        propagateWindFillIn(map.get(x - 1, y), map.get(x - 2, y));
        propagateWindFillIn(map.get(x + 1, y), map.get(x + 2, y));
        propagateWindFillIn(map.get(x, y - 1), map.get(x, y - 2));
        propagateWindFillIn(map.get(x, y + 1), map.get(x, y + 2));
    }

    public void finishUpdate() {
        wind = _newWind;
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

    private void useWind() {
        for (Resource resource : resourcePack.getResources()) {
            if (!resource.getGenome().isMovable()) {
                continue;
            }
            double overallWindLevel = wind.affectedTiles.stream()
                    .map(Pair<Tile, Double>::getSecond)
                    .reduce(Double::sum)
                    .orElse(0.0);
            for (Pair<Tile, Double> pair : wind.affectedTiles) {
                int part = (int) (resource.getAmount() * pair.getSecond() / overallWindLevel *
                        Math.min(pair.getSecond() * 0.0001 / resource.getGenome().getMass(), 1));
                if (part > 0) {
                    pair.getFirst().addDelayedResource(resource.getCleanPart(part));
                }
            }
        }
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
            double level = tile.wind.getPureLevelByTile(this) - SpaceData.INSTANCE.getData().getWindPropagation();
            if (level > 0) {
                _newWind.changeLevelOnTile(target, level);
            }
        }
    }

    private void propagateWindFillIn(Tile tile, Tile target) {
        if (tile != null && target != null) {
            double level = tile.wind.getLevelByTile(target) - session.windFillIn;
            if (level > 0) {
                _newWind.isFilling = true;
                _newWind.changeLevelOnTile(tile, level);
            }
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
