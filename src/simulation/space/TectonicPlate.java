package simulation.space;

import extra.ProbFunc;
import extra.ShnyPair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TectonicPlate extends Territory {
    public enum Direction {
        U(-1, 0),
        D(1, 0),
        L(0, -1),
        R(0, 1);

        ShnyPair<Integer, Integer> vector;
        Direction(int x, int y) {vector = new ShnyPair<>(x, y);}
    }
    public enum Type {
        Terrain,
        Oceanic
    }
    private Direction direction;
    private Type type;
    TectonicPlate() {
        direction = Direction.values()[ProbFunc.randomInt(Direction.values().length)];
        type = Type.values()[ProbFunc.randomInt(Type.values().length)];
    }

    public void initialize() {
        if (type == Type.Terrain) {
            return;
        }
        for (Tile tile: getTiles()) {
            tile.type = Tile.Type.Water;
        }
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void addTile(Tile tile) {
        super.addTile(tile);
        tile.setPlate(this);
    }

    public Direction getDirection() {
        return direction;
    }

    public List<Tile> getAffectedTiles() {
        List<Tile> startTiles = getBrinkWithCondition(tile -> {
            Tile t;
            switch (direction) {
                case U:
                    t = tile.getWorld().map.get(tile.x + 1, tile.y);
                    return t!= null && t.getPlate() == this;
                case R:
                    t = tile.getWorld().map.get(tile.x, tile.y - 1);
                    return t!= null && t.getPlate() == this;
                case L:
                    t = tile.getWorld().map.get(tile.x, tile.y + 1);
                    return t!= null && t.getPlate() == this;
                case D:
                    t = tile.getWorld().map.get(tile.x - 1, tile.y);
                    return t!= null && t.getPlate() == this;
                default:
                    return false;
            }
        });
        List<Tile> affectedTiles = new ArrayList<>();
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
            affectedTiles.addAll(neighbours);
        }
        return affectedTiles.stream().distinct().collect(Collectors.toList());
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

    public void move() {
        for (Tile tile: getAffectedTiles()) {
            tile.type = Tile.Type.Mountain;
        }
    }
}
