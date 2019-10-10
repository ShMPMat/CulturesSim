package simulation.culture.group;

import simulation.space.Territory;
import simulation.space.Tile;
import simulation.space.resource.ResourcePack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static extra.ProbFunc.*;

public class PlacementStrategy {
    enum Strategy {
        DoNothing,
        OneTile,
        Border,
        Homogeneous,
        Sprinkle,
        Clumps//TODO large clumps; nets
    }
    private Strategy strategy;
    private Territory controlledTerritory;
    private List<Tile> specialTiles;

    PlacementStrategy(Territory controlledTerritory) {
        this.controlledTerritory = controlledTerritory;
        this.specialTiles = new ArrayList<>();
    }

    PlacementStrategy(Territory controlledTerritory, Strategy strategy) {
        this(controlledTerritory);
        switch (strategy) {
            case Sprinkle:
            case OneTile:
            case Clumps:
                specialTiles.add(randomTile(controlledTerritory));
                break;
        }
        this.strategy = strategy;
    }

    PlacementStrategy(Territory controlledTerritory, Strategy strategy, Collection<Tile> tiles) {
        this(controlledTerritory, strategy);
        this.specialTiles.addAll(tiles);
    }

    private Tile getTileForPlacement(ResourcePack resourcePack) {
        List<Tile> tiles;
        switch (strategy) {
            case DoNothing:
                return null;
            case OneTile:
                return specialTiles.get(0);
            case Border:
                List<Tile> border = controlledTerritory.getBorder();
                tiles = border.stream().filter(tile ->
                        !tile.getResources().containsAll(resourcePack.resources)).collect(Collectors.toList());
                return tiles.isEmpty() ? border.get(randomInt(border.size())) : tiles.get(randomInt(tiles.size()));
            case Homogeneous:
                tiles = controlledTerritory.getTiles().stream().filter(tile ->
                        !tile.getResources().containsAll(resourcePack.resources)).collect(Collectors.toList());
                return tiles.isEmpty() ? randomTile(controlledTerritory) : tiles.get(randomInt(tiles.size()));
            case Sprinkle:
                return chooseSpecialTile();
            case Clumps:
                Tile tile = chooseSpecialTile();
                tiles = tile.getNeighbours(t -> true);
                tiles.add(tile);
                return tiles.get(randomInt(tiles.size()));
        }
        return null;
    }

    private Tile chooseSpecialTile() {
        int index = randomInt(specialTiles.size() + 1);
        if (index < specialTiles.size()) {
            return specialTiles.get(index);
        }
        for (int i = 0; i < 5; i++) {
            Tile tile = randomTile(controlledTerritory);
            if (!controlledTerritory.contains(tile)) {
                specialTiles.add(tile);
                return tile;
            }
        }
        if (specialTiles.size() == 0) {
            int i = 0;
        }
        return specialTiles.get(randomInt(specialTiles.size()));
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
