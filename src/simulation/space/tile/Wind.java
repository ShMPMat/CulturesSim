package simulation.space.tile;

import kotlin.Pair;
import simulation.space.SpaceData;

import java.util.ArrayList;
import java.util.List;

public class Wind {
    boolean isFilling = false;
    List<Pair<Tile, Double>> affectedTiles;

    public Wind() {
        affectedTiles = new ArrayList<>();
    }

    public List<Pair<Tile, Double>> getAffectedTiles() {
        return affectedTiles;
    }

    public boolean isStill() {
        return affectedTiles.isEmpty();
    }

    public double getMaxLevel() {
        return affectedTiles.stream()
                .map(Pair<Tile, Double>::getSecond)
                .reduce(Double::max)
                .orElse(0.0);
    }

    public void changeLevelOnTile(Tile tile, double change) {
        if (tile == null) {//TODO da hell?
            return;
        }
        for (int i = 0; i < affectedTiles.size(); i++) {
            Pair<Tile, Double> pair = affectedTiles.get(i);
            if (pair.getFirst().equals(tile)) {
                pair = new Pair<>(
                        pair.getFirst(),
                        Math.min(change + pair.getSecond(), SpaceData.INSTANCE.getData().getMaximalWind())
                );
                if (pair.getSecond() <= 0) {
                    affectedTiles.remove(i);
                }
                return;
            }
        }
        if (change <= 0) {
            return;
        }
        affectedTiles.add(new Pair<>(tile, change));
    }

    public double getLevelByTile(Tile tile) {
        for (Pair<Tile, Double> pair: affectedTiles) {
            if (pair.getFirst().equals(tile)) {
                return pair.getSecond();
            }
        }
        return affectedTiles.stream()
                .filter(p -> p.getFirst().equals(tile))
                .map(Pair<Tile, Double>::getSecond)
                .findFirst()
                .orElse(0.0);
    }

    public double getPureLevelByTile(Tile tile) {
        if (isFilling) {
            return 0;
        }
        return getLevelByTile(tile);
    }
}
