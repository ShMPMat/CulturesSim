package simulation.space;

import extra.ShnyPair;
import kotlin.collections.ArraysKt;
import simulation.Controller;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static shmp.random.RandomCollectionsKt.*;
import static shmp.random.RandomProbabilitiesKt.*;
import static simulation.Controller.session;

public class TectonicPlate extends Territory {
    /**
     * Enum with all possible directions in which Plate can move.
     */
    public enum Direction {
        U(-1, 0),
        D(1, 0),
        L(0, -1),
        R(0, 1);

        ShnyPair<Integer, Integer> vector;
        Direction(int x, int y) {vector = new ShnyPair<>(x, y);}
    }

    /**
     * Enum with all possible Types of the Plate.
     */
    public enum Type {
        Terrain,
        Oceanic
    }

    /**
     * In which Direction this Plate moves.
     */
    private Direction direction;
    /**
     * Type of this Plate.
     */
    private Type type;
    /**
     * Which Tiles are affected by this Plate movement.
     */
    private List<ShnyPair<Tile, Double>> affectedTiles;
    /**
     * Whether it was moved for the first time.
     */
    private boolean isMoved = false;

    TectonicPlate() {
        direction = randomElement(ArraysKt.toList(Direction.values()), session.random);
        type = randomElement(ArraysKt.toList(Type.values()), session.random);
    }

    /**
     * Changes Plate's Tiles depending on its Type.
     */
    public void initialize() {
        if (type == Type.Terrain) {
            return;
        }
        for (Tile tile: getTiles()) {
            tile.setType(Tile.Type.Water, true);
        }
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void add(Tile tile) {
        super.add(tile);
        tile.setPlate(this);
    }

    public Direction getDirection() {
        return direction;
    }

    /**
     * @return Tiles affected by this Plate.
     */
    public List<ShnyPair<Tile, Double>> getAffectedTiles() {
        if (affectedTiles != null) {
            return affectedTiles;
        }
        List<Tile> startTiles = getBrinkWithCondition(tile -> {
            Tile t;
            switch (direction) {
                case U:
                    t = session.world.map.get(tile.x + 1, tile.y);
                    return t!= null && t.getPlate() == this;
                case R:
                    t = session.world.map.get(tile.x, tile.y - 1);
                    return t!= null && t.getPlate() == this;
                case L:
                    t = session.world.map.get(tile.x, tile.y + 1);
                    return t!= null && t.getPlate() == this;
                case D:
                    t = session.world.map.get(tile.x - 1, tile.y);
                    return t!= null && t.getPlate() == this;
                default:
                    return false;
            }
        });
        List<Tile> tiles = new ArrayList<>();
        for (Tile tile: startTiles) {
            List<Tile> neighbours = new ArrayList<>(), newTiles = new ArrayList<>();
            neighbours.add(tile);
            newTiles.add(tile);
            for (int i = 0; i < getInteractionCoof(tile.getPlate()); i++) {
                for (Tile n: neighbours) {
                    newTiles.addAll(n.getNeighbours(t -> !newTiles.contains(t)));
                }
                neighbours.clear();
                neighbours.addAll(newTiles);
            }
            tiles.addAll(neighbours);
        }
        this.affectedTiles = tiles.stream().map(tile -> new ShnyPair<>(tile, (Math.random() + 0.1)/1.1)).collect(Collectors.toList());
        return this.affectedTiles;
    }

    private int getInteractionCoof(TectonicPlate tectonicPlate) {
        ShnyPair<Integer, Integer> result = new ShnyPair<>(Math.abs(direction.vector.first - tectonicPlate.direction.vector.first),
                Math.abs(direction.vector.second - tectonicPlate.direction.vector.second));
        if (result.first == 0 && result.second == 0) {
            return 0;
        } else if (result.first <= 1 && result.second <= 1) {
            return 1;
        }
        return 2;
    }

    /**
     * Moves plate in its direction and changes landscape.
     */
    public void move() {//TODO volcanoes
        if (testProbability(0.7, session.random) && isMoved) {
            return;
        }
        for (ShnyPair<Tile, Double> pair: getAffectedTiles()) {
            if (testProbability(pair.second, session.random)) {
                pair.first.setLevel(isMoved ? pair.first.getLevel() + 1 :
                        pair.first.getLevel() + 5 + session.random.nextInt(5));
            }
        }
        isMoved = true;
    }
}
