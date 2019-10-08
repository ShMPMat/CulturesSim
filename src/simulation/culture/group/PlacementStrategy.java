package simulation.culture.group;

import simulation.space.Territory;
import simulation.space.Tile;
import simulation.space.resource.ResourcePack;

import java.util.List;
import java.util.stream.Collectors;

import static extra.ProbFunc.*;

public class PlacementStrategy {
    enum Strategy {
        DoNothing,
        OneTile,
        Border,
        Homogeneous
    }
    private Strategy strategy;
    private Territory controlledTerritory;
    private Tile oneTile = null;

    PlacementStrategy(Territory controlledTerritory) {
        this.controlledTerritory = controlledTerritory;
    }

    PlacementStrategy(Territory controlledTerritory, Strategy strategy) {
        this(controlledTerritory);
        if (strategy == Strategy.OneTile) {
            oneTile = randomTile(controlledTerritory);
        }
        this.strategy = strategy;
    }

    PlacementStrategy(Territory controlledTerritory, Tile tile) {
        this(controlledTerritory);
        this.strategy = Strategy.OneTile;
        oneTile = tile;
    }

    private Tile getTileForPlacement(ResourcePack resourcePack) {
        List<Tile> tiles;
        switch (strategy) {
            case DoNothing:
                return null;
            case OneTile:
                return oneTile;
            case Border:
                List<Tile> border = controlledTerritory.getBorder();
                tiles = border.stream().filter(tile ->
                        !tile.getResources().containsAll(resourcePack.resources)).collect(Collectors.toList());
                return tiles.isEmpty() ? border.get(randomInt(border.size())) : tiles.get(randomInt(tiles.size()));
            case Homogeneous:
                tiles = controlledTerritory.getTiles().stream().filter(tile ->
                        !tile.getResources().containsAll(resourcePack.resources)).collect(Collectors.toList());
                return tiles.isEmpty() ? randomTile(controlledTerritory) : tiles.get(randomInt(tiles.size()));
        }
        return null;
    }

    void place(ResourcePack resourcePack) {
        Tile tile = getTileForPlacement(resourcePack);
        if (tile != null) {
            resourcePack.resources.forEach(tile::addDelayedResource);
        }
    }

    @Override
    public String toString() {
        return strategy.toString();
    }
}
