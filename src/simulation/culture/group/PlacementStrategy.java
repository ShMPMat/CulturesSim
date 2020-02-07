package simulation.culture.group;

import extra.ProbabilityFuncs;
import extra.SpaceProbabilityFuncs;
import simulation.space.Territory;
import simulation.space.Tile;
import simulation.space.resource.ResourcePack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static extra.ProbabilityFuncs.*;

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
                specialTiles.add(SpaceProbabilityFuncs.randomTile(controlledTerritory));
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
                return tiles.isEmpty() ? randomElement(border) : randomElement(tiles);
            case Homogeneous:
                tiles = controlledTerritory.getTiles().stream().filter(tile ->
                        !tile.getResources().containsAll(resourcePack.resources)).collect(Collectors.toList());
                return tiles.isEmpty()
                        ? SpaceProbabilityFuncs.randomTile(controlledTerritory)
                        : ProbabilityFuncs.randomElement(tiles);
            case Sprinkle:
                return chooseSpecialTile();
            case Clumps:
                Tile tile = chooseSpecialTile();
                tiles = tile.getNeighbours();
                tiles.add(tile);
                return ProbabilityFuncs.randomElement(tiles);
        }
        return null;
    }

    private Tile chooseSpecialTile() {
        int index = randomInt(specialTiles.size() + 1);
        if (index < specialTiles.size()) {
            return specialTiles.get(index);
        }
        for (int i = 0; i < 5; i++) {
            Tile tile = SpaceProbabilityFuncs.randomTile(controlledTerritory);
            if (!controlledTerritory.contains(tile)) {
                specialTiles.add(tile);
                return tile;
            }
        }
        if (specialTiles.size() == 0) {
            int i = 0;
        }
        return ProbabilityFuncs.randomElement(specialTiles);
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
