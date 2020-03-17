package simulation.space;

import simulation.space.resource.Resource;
import simulation.space.tile.Tile;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents tile map of the world
 */
public class WorldMap {
    private List<List<Tile>> tiles;
    private int x;
    private int y;
    private List<TectonicPlate> tectonicPlates = new ArrayList<>();

    public WorldMap(List<List<Tile>> tiles) {
        this.tiles = tiles;
        x = tiles.size();
        y = tiles.get(0).size();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<List<Tile>> getTiles() {
        return tiles;
    }

    public List<TectonicPlate> getTectonicPlates() {
        return tectonicPlates;
    }

    public void addPlate(TectonicPlate plate) {
        tectonicPlates.add(plate);
    }

    public Tile get(int x, int y) {
        if (x < 0) {
            return null;
        } else if (x >= getX()) {
            return null;
        }
        while (y < 0) {
            y += getY();
        }
        y %= getY();
        return getTiles().get(x).get(y);
    }

    public List<Resource> getAllResourceInstancesWithName(String name) {
        return tiles.stream()
                .flatMap(Collection::stream)
                .flatMap(t -> t.getResourcePack().getResources().stream())
                .filter(r -> r.getBaseName().equals(name))
                .collect(Collectors.toList());
    }

    /**
     * @param predicate the Predicate on which Tiles will be tested.
     * @return All Tiles from the map which satisfy the Predicate.
     */
    public List<Tile> getTilesWithPredicate(Predicate<Tile> predicate) {
        return tiles.stream()
                .flatMap(Collection::stream)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public synchronized void update() {//TODO parallel
        for (List<Tile> line : getTiles()) {
            for (Tile tile : line) {
                tile.startUpdate();
            }
        }

        for (List<Tile> line : getTiles()) {
            for (Tile tile : line) {
                tile.middleUpdate();
            }
        }
    }

    public synchronized void finishUpdate() {
        for (List<Tile> line : getTiles()) {
            for (Tile tile : line) {
                tile.finishUpdate();
            }
        }
    }

    public void geologicUpdate() {
        for (List<Tile> line : getTiles()) {
            for (Tile tile : line) {
                tile.levelUpdate();
            }
        }
        platesUpdate();
    }

    public void platesUpdate() {
        for (TectonicPlate plate : getTectonicPlates()) {
            plate.move();
        }
    }
}
