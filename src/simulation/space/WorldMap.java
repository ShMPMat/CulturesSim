package simulation.space;

import extra.SpaceProbabilityFuncs;
import shmp.random.RandomCollectionsKt;
import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourceIdeal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static simulation.Controller.*;

/**
 * Represents tile map of the world
 */
public class WorldMap {
    List<Boolean> _execution;
    /**
     * Array which contains Map Tiles.
     */
    public List<List<Tile>> map;
    private List<TectonicPlate> tectonicPlates;

    public WorldMap(int x, int y) {
        createTiles(x, y);
    }

    private void createTiles(int x, int y) {
        map = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            map.add(new ArrayList<>());
            for (int j = 0; j < y; j++) {
                map.get(i).add(new Tile(i, j));
            }
        }
    }

    public void initializePlates() {
        Set<Tile> usedTiles = new HashSet<>();
        tectonicPlates = new ArrayList<>();
        for (int i = 0; i < session.amountOfPlates; i++) {
            TectonicPlate tectonicPlate = new TectonicPlate();
            Tile tile = SpaceProbabilityFuncs.randomTile(this);
            while (usedTiles.contains(tile)) {
                tile = SpaceProbabilityFuncs.randomTile(this);
            }
            tectonicPlate.add(tile);
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
                Tile tile = RandomCollectionsKt.randomElement(tiles, session.random);
                territory.add(tile);
                usedTiles.add(tile);
                sw = true;
            }
        }
    }

    public Tile get(int x, int y) {
        if (x < 0) {
            return null;
        } else if (x >= session.mapSizeX) {
            return null;
        }
        while (y < 0) {
            y += session.mapSizeY;
        }
        y %= session.mapSizeY;
        return map.get(x).get(y);
    }

    public Set<Group> getAllNearGroups(Group group) {
        Set<Group> groups = new HashSet<>();
        for (Tile tile : group.getTiles()) {
            tile.getNeighbours(t -> t.group != null).forEach(t -> groups.add(t.group));
        }
        groups.remove(group);
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
        for (TectonicPlate plate : getTectonicPlates()) {
            plate.move();
        }
    }
}
