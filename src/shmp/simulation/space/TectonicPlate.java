package shmp.simulation.space;

import kotlin.Pair;
import shmp.simulation.space.territory.BrinkInvariantTerritory;
import shmp.simulation.space.tile.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static shmp.random.RandomProbabilitiesKt.*;
import static shmp.simulation.Controller.session;

public class TectonicPlate extends BrinkInvariantTerritory {
    /**
     * Enum with all possible directions in which Plate can move.
     */
    public enum Direction {
        U(-1, 0),
        D(1, 0),
        L(0, -1),
        R(0, 1);

        Pair<Integer, Integer> vector;
        Direction(int x, int y) {vector = new Pair<>(x, y);}
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
    private List<Pair<Tile, Double>> affectedTiles;
    /**
     * Whether it was moved for the first time.
     */
    private boolean isMoved = false;

    public TectonicPlate(Direction direction, Type type) {
        this.direction = direction;
        this.type = type;
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
    public List<Pair<Tile, Double>> getAffectedTiles() {
        if (affectedTiles != null) {
            return affectedTiles;
        }
        List<Tile> startTiles = filterOuterBrink(tile -> {
            List<Tile> affectedTiles = new ArrayList<>();
            switch (direction) {
                case U:
                    affectedTiles = tile.getNeighbours(t -> t.getX() == tile.getX() + 1 && t.getY() == tile.getY());
                    break;
                case R:
                    affectedTiles = tile.getNeighbours(t -> t.getX() == tile.getX() && t.getY() == tile.getY() - 1);
                    break;
                case L:
                    affectedTiles = tile.getNeighbours(t -> t.getX() == tile.getX() && t.getY() == tile.getY() + 1);
                    break;
                case D:
                    affectedTiles = tile.getNeighbours(t -> t.getX() == tile.getX() - 1 && t.getY() == tile.getY());
                    break;
            }
            return !affectedTiles.isEmpty() && affectedTiles.get(0).getPlate() == this;
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
        this.affectedTiles = tiles.stream()
                .map(tile -> new Pair<>(tile, (session.random.nextDouble() + 0.1)/1.1))
                .collect(Collectors.toList());
        return this.affectedTiles;
    }

    private int getInteractionCoof(TectonicPlate tectonicPlate) {
        Pair<Integer, Integer> result = new Pair<>(
                Math.abs(direction.vector.getSecond() - tectonicPlate.direction.vector.getSecond()),
                Math.abs(direction.vector.getSecond() - tectonicPlate.direction.vector.getSecond())
        );
        if (result.getFirst() == 0 && result.getFirst() == 0) {
            return 0;
        } else if (result.getFirst() <= 1 && result.getFirst() <= 1) {
            return SpaceData.INSTANCE.getData().getTectonicRange() / 2;
        }
        return SpaceData.INSTANCE.getData().getTectonicRange();
    }

    /**
     * Moves plate in its direction and changes landscape.
     */
    public void move() {//TODO volcanoes
        if (testProbability(0.7, session.random) && isMoved) {
            return;
        }
        for (Pair<Tile, Double> pair: getAffectedTiles()) {
            if (testProbability(pair.getSecond(), session.random)) {
                pair.getFirst().setLevel(isMoved ? pair.getFirst().getLevel() + 1 :
                        pair.getFirst().getLevel() + 5 + session.random.nextInt(5));
            }
        }
        isMoved = true;
    }
}
