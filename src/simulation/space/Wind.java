package simulation.space;

import extra.ShnyPair;

import java.util.ArrayList;
import java.util.List;

public class Wind {
    static double windPropagation = 0.025;
    static double maximalLevel = 10;
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

    public void changeLevelOnTile(Tile tile, double change) {
        if (tile == null) {
            return;
        }
        for (int i = 0; i < affectedTiles.size(); i++) {
            ShnyPair<Tile, Double> pair = affectedTiles.get(i);
            if (pair.first.equals(tile)) {
                pair.second = Math.min(change + pair.second, maximalLevel);
                if (pair.second <= 0) {
                    affectedTiles.remove(i);
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
