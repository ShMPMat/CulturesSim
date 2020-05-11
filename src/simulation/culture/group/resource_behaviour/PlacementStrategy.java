package simulation.culture.group.resource_behaviour;

import extra.SpaceProbabilityFuncs;
import simulation.space.Territory;
import simulation.space.tile.Tile;
import simulation.space.resource.MutableResourcePack;

import java.util.ArrayList;
import java.util.List;

import static shmp.random.RandomCollectionsKt.*;
import static simulation.Controller.*;

public class PlacementStrategy {
    public enum Strategy {
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

    private Tile getTileForPlacement(MutableResourcePack resourcePack) {
        List<Tile> tiles;
        switch (strategy) {
            case DoNothing:
                return null;
            case OneTile:
                return specialTiles.get(0);
            case Border:
                List<Tile> border = controlledTerritory.getInnerBrink();
                List<Tile> unfinishedBorder = controlledTerritory
                        .getInnerBrink(t -> !t.getResourcePack().containsAll(resourcePack));
                return unfinishedBorder.isEmpty()
                        ? randomElement(border, session.random)
                        : randomElement(unfinishedBorder, session.random);
            case Homogeneous:
                tiles = controlledTerritory.getTiles(t -> !t.getResourcePack().containsAll(resourcePack));
                return tiles.isEmpty()
                        ? SpaceProbabilityFuncs.randomTile(controlledTerritory)
                        : randomElement(tiles, session.random);
            case Sprinkle:
                return chooseSpecialTile();
            case Clumps:
                Tile tile = chooseSpecialTile();
                tiles = tile.getNeighbours();
                tiles.add(tile);
                return randomElement(tiles, session.random);
        }
        return null;
    }

    private Tile chooseSpecialTile() {
        int index = session.random.nextInt(specialTiles.size() + 1);
        if (index < specialTiles.size()) {
            return specialTiles.get(index);
        }
        for (int i = 0; i < 5; i++) {
            try {
                Tile tile = SpaceProbabilityFuncs.randomTile(controlledTerritory);
                if (!controlledTerritory.contains(tile)) {
                    specialTiles.add(tile);
                    return tile;
                }
            } catch (Exception e) {
                int b = 0;
            }
        }
        return randomElement(specialTiles, session.random);
    }

    public void place(MutableResourcePack resourcePack) {
        Tile tile = getTileForPlacement(resourcePack);
        if (tile != null) {
            tile.addDelayedResources(resourcePack);
        }
    }

    @Override
    public String toString() {
        return strategy.toString();
    }
}
