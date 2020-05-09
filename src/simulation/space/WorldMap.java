package simulation.space;

import simulation.Controller;
import simulation.space.tile.Tile;
import simulation.space.tile.TileTagSetterKt;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static simulation.Controller.*;

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

    public List<List<Tile>> getLinedTiles() {
        return tiles;
    }

    public List<TectonicPlate> getTectonicPlates() {
        return tectonicPlates;
    }

    public void addPlate(TectonicPlate plate) {
        tectonicPlates.add(plate);
    }

    public Tile get(int x, int y) {
        if (SpaceData.INSTANCE.getData().getXMapLooping()) {
            x = cutCoordinate(x, getX());
        } else if (!checkCoordinate(x, getX())) {
            return null;
        }
        if (SpaceData.INSTANCE.getData().getYMapLooping()) {
            y = cutCoordinate(y, getY());
        } else if (!checkCoordinate(y, getY())) {
            return null;
        }
        return getLinedTiles().get(x).get(y);
    }

    private int cutCoordinate(int coordinate, int max) {
        if (coordinate < 0) {
            coordinate = coordinate % max + max;
        }
        return coordinate % max;
    }

    private boolean checkCoordinate(int coordinate, int max) {
        return coordinate >= 0 && coordinate < max;
    }

    public void setTags() {
        int name = 0;
        List<Tile> allTiles = getTiles();
        for (Tile tile : allTiles) {
            if (TileTagSetterKt.setTags(tile, "" + name)) {
                name++;
            }
        }
    }

    public List<Tile> getTiles() {
        return tiles.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<Tile> getTiles(Predicate<Tile> predicate) {
        return tiles.stream()
                .flatMap(Collection::stream)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public synchronized void update() {//TODO parallel
        for (List<Tile> line : getLinedTiles()) {
            for (Tile tile : line) {
                tile.startUpdate();
            }
        }

        for (List<Tile> line : getLinedTiles()) {
            for (Tile tile : line) {
                tile.middleUpdate();
            }
        }
    }

    public synchronized void finishUpdate() {
        for (List<Tile> line : getLinedTiles()) {
            for (Tile tile : line) {
                tile.finishUpdate();
            }
        }
    }

    public void geologicUpdate() {
        for (List<Tile> line : getLinedTiles()) {
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
