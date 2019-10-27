package simulation.space;

import extra.ShnyPair;

import java.util.ArrayList;
import java.util.List;

public class Wind {
    static double windPropagation = 0.025;
    List<ShnyPair<Tile, Double>> affectedTiles;

    public Wind() {
        affectedTiles = new ArrayList<>();
    }

    public List<ShnyPair<Tile, Double>> getAffectedTiles() {
        return affectedTiles;
    }

    public boolean isStill() {
        return affectedTiles.isEmpty();
    }

    public void changeLevelOnTile(Tile tile, double change) {//TODO remove if change makes level negative
        if (tile == null) {
            return;
        }
        for (ShnyPair<Tile, Double> pair: affectedTiles) {
            if (pair.first.equals(tile)) {
                pair.second += change;
                if (pair.second > 30) { //TODO strong winds
                    int i = 0;
                }
                return;
            }
        }
        if (change <= 0) {
            return;
        }
        affectedTiles.add(new ShnyPair<>(tile, change));
    }

    public double getLevelByTile(Tile tile) {
        for (ShnyPair<Tile, Double> pair: affectedTiles) {
            if (pair.first.equals(tile)) {
                return pair.second;
            }
        }
        return 0;
    }
}
