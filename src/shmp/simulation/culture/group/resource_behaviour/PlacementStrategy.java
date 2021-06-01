package shmp.simulation.culture.group.resource_behaviour;

import shmp.simulation.space.territory.Territory;
import shmp.simulation.space.tile.Tile;
import shmp.simulation.space.resource.container.MutableResourcePack;

import java.util.ArrayList;
import java.util.List;

import static shmp.random.RandomElementKt.*;
import static shmp.random.RandomSpaceKt.randomTile;
import static shmp.simulation.CulturesController.*;

public class PlacementStrategy {
    public enum Strategy {
        DoNothing,
        OneTile,
        Border,
        Homogeneous,
        Sprinkle
    }
    private Strategy strategy;
    private List<Tile> specialTiles;

    PlacementStrategy() {
        this.specialTiles = new ArrayList<>();
    }

    PlacementStrategy(Strategy strategy) {
        this();
        switch (strategy) {
            case Sprinkle:
            case OneTile:
        }
        this.strategy = strategy;
    }

    private Tile getTileForPlacement(MutableResourcePack resourcePack, Territory controlledTerritory) {
        List<Tile> tiles;
        switch (strategy) {
            case DoNothing:
                return null;
            case OneTile:
                if (specialTiles.isEmpty()) {
                    chooseSpecialTile(controlledTerritory);
                }
                if (specialTiles.isEmpty()) {
                    return null;
                }
                return specialTiles.get(0);
            case Border:
                List<Tile> border = controlledTerritory.getInnerBrink();
                List<Tile> unfinishedBorder = controlledTerritory
                        .filterInnerBrink(t -> !t.getResourcePack().containsAll(resourcePack));
                return unfinishedBorder.isEmpty()
                        ? randomElementOrNull(border, session.random)
                        : randomElementOrNull(unfinishedBorder, session.random);
            case Homogeneous:
                tiles = controlledTerritory.filter(t -> !t.getResourcePack().containsAll(resourcePack));
                return tiles.isEmpty()
                        ? randomTile(controlledTerritory)
                        : randomElement(tiles, session.random);
            case Sprinkle:
                return chooseSpecialTile(controlledTerritory);
        }
        return null;
    }

    private Tile chooseSpecialTile(Territory controlledTerritory) {
        int index = session.random.nextInt(specialTiles.size() + 1);
        if (index < specialTiles.size()) {
            return specialTiles.get(index);
        }
        for (int i = 0; i < 5; i++) {
            try {
                Tile tile = randomTile(controlledTerritory);
                if (!controlledTerritory.contains(tile)) {
                    specialTiles.add(tile);
                    return tile;
                }
            } catch (Exception e) {
                int b = 0;
            }
        }
        return randomElementOrNull(specialTiles, session.random);
    }

    public void place(MutableResourcePack resourcePack, Territory territory) {
        Tile tile = getTileForPlacement(resourcePack, territory);
        if (tile != null) {
            tile.addDelayedResources(resourcePack);
        }
    }

    @Override
    public String toString() {
        return strategy.toString();
    }
}
