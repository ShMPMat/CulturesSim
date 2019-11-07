package simulation.space;

import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourceIdeal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static extra.ProbFunc.*;

/**
 * Represents tile map of the world
 */
public class WorldMap {
    List<Boolean> _execution;
    /**
     * Array which contains Map Tiles.
     */
    public List<List<Tile>> map;
    public List<TectonicPlate> tectonicPlates;
    public List<ResourceIdeal> resourcePool;
    private int amountOfPlates = 10;

    public WorldMap(int x, int y, List<ResourceIdeal> resources) {
        this.resourcePool = resources;
        map = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            map.add(new ArrayList<>());
            for (int j = 0; j < y; j++) {
                map.get(i).add(new Tile(i, j));
            }
        }
    }

    public void initializePlates() {
        List<Tile> usedTiles = new ArrayList<>();
        tectonicPlates = new ArrayList<>();
        for (int i = 0; i < amountOfPlates; i++) {
            TectonicPlate tectonicPlate = new TectonicPlate();
            Tile tile = randomTile(this);
            while (usedTiles.contains(tile)) {
                tile = randomTile(this);
            }
            tectonicPlate.addTile(tile);
            tectonicPlates.add(tectonicPlate);
            usedTiles.add(tile);
        }
        boolean sw = true;
        while (sw) {
            sw = false;
            for (Territory territory : tectonicPlates) {
                List<Tile> tiles = territory.getBrinkWithCondition(t -> !usedTiles.contains(t));
                if (tiles.isEmpty()) {
                    continue;
                }
                Tile tile = randomElement(tiles);
                territory.addTile(tile);
                usedTiles.add(tile);
                sw = true;
            }
        }
    }

    /**
     *
     * @param x X coordinate of the Tile.
     * @param y Y coordinate os the Tile.
     * @return Tile on this coordinates or null if coordinates are out of bounds.
     */
    public Tile get(int x, int y) {
        try {
            if (y < 0) {
                y = map.get(0).size() + y;
            } else if (y >= map.get(0).size()) {
                y -= map.get(0).size();
            }
            return map.get(x).get(y);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public Set<Group> getAllNearGroups(Group group) {
        Set<Group> groups = new HashSet<>();
        for (Tile tile : group.getTiles()) {
            int x = tile.x, y = tile.y;
            if (get(x, y + 1) != null && get(x, y + 1).group != null && get(x, y + 1).group != group) {
                groups.add(get(x, y + 1).group);
            }
            if (get(x, y - 1) != null && get(x, y - 1).group != null && get(x, y - 1).group != group) {
                groups.add(get(x, y - 1).group);
            }
            if (get(x + 1, y) != null && get(x + 1, y).group != null && get(x + 1, y).group != group) {
                groups.add(get(x + 1, y).group);
            }
            if (get(x - 1, y) != null && get(x - 1, y).group != null && get(x - 1, y).group != group) {
                groups.add(get(x - 1, y).group);
            }
        }
        return groups;
    }

    public List<Resource> getAllResourceInstancesWithName(String name) {
        List<Resource> resources = new ArrayList<>();
        for (List<Tile> line : map) {
            for (Tile tile : line) {
                resources.addAll(tile.getResources().stream().filter(resource -> resource.getBaseName().equals(name))
                        .collect(Collectors.toList()));
            }
        }
        return resources;
    }

    public List<TectonicPlate> getTectonicPlates() {
        return tectonicPlates;
    }

    /**
     * @param predicate the Predicate on which Tiles will be tested.
     * @return All Tiles from the map which satisfy the Predicate.
     */
    public List<Tile> getTilesWithPredicate(Predicate<Tile> predicate) {
        List<Tile> goodTiles = new ArrayList<>();
        for (List<Tile> tiles : map) {
            goodTiles.addAll(tiles.stream().filter(predicate).collect(Collectors.toList()));
        }
        return goodTiles;
    }

    public synchronized void update() {//TODO parallel
//        int i = 0, a = 5, ct = 0;
//        _execution = new CopyOnWriteArrayList<Boolean>();
//        for (int j = 0; j < (map.size() - 1) / a + 1; j++) {
//            _execution.add(false);
//        }
//        while (i < map.size()) {
//            new Thread(new MapUpdater(ct, i, i + a, this)).start();
//            i += a;
//            ct++;
//        }
//        try {
//            for (int j = 0; j < _execution.size(); j++) {
//                while (!_execution.get(j)) {
//                    wait();
//                }
//            }
//        } catch (InterruptedException e) {
//
//        }
        for (List<Tile> line : map) {
            for (Tile tile : line) {
                tile.startUpdate();
            }
        }

        for (List<Tile> line : map) {
            for (Tile tile : line) {
                tile.middleUpdate();
            }
        }
    }

    public void geologicUpdate() {
        for (List<Tile> line : map) {
            for (Tile tile : line) {
                tile.levelUpdate();
            }
        }
        movePlates();
    }

    public synchronized void finishUpdate() {
        for (List<Tile> line : map) {
            for (Tile tile : line) {
                tile.finishUpdate();
            }
        }
    }

    /**
     * MOves all the Plates on the Map.
     */
    public void movePlates() {
        for (TectonicPlate plate: getTectonicPlates()) {
            plate.move();
        }
    }
}
