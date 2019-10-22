package simulation.space;

import extra.ShnyPair;

import java.util.ArrayList;
import java.util.List;

public class Wind {
    List<ShnyPair<Tile, Integer>> affectedTiles;

    public Wind() {
        affectedTiles = new ArrayList<>();
    }

    public List<ShnyPair<Tile, Integer>> getAffectedTiles() {
        return affectedTiles;
    }

    public boolean isStill() {
        return affectedTiles.isEmpty();
    }

    public void changeLevelOnTile(Tile tile, int change) {//TODO remove if change makes level negative
        if (tile == null) {
            return;
        }
        for (ShnyPair<Tile, Integer> pair: affectedTiles) {
            if (pair.first.equals(tile)) {
                pair.second += change;
                return;
            }
        }
        if (change <= 0) {
            return;
        }
        affectedTiles.add(new ShnyPair<>(tile, change));
    }

    public int getLevelByTile(Tile tile) {
        for (ShnyPair<Tile, Integer> pair: affectedTiles) {
            if (pair.first.equals(tile)) {
                return pair.second;
            }
        }
        return 0;
    }
}
