package simulation.culture.group.resource_behaviour;

import extra.SpaceProbabilityFuncs;
import shmp.random.RandomCollectionsKt;
import simulation.Controller;
import simulation.space.Territory;
import simulation.space.Tile;
import simulation.space.resource.ResourcePack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
                return tiles.isEmpty()
                        ? randomElement(border, session.random)
                        : randomElement(tiles, session.random);
            case Homogeneous:
                tiles = controlledTerritory.getTiles().stream().filter(tile ->
                        !tile.getResources().containsAll(resourcePack.resources)).collect(Collectors.toList());
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
            Tile tile = SpaceProbabilityFuncs.randomTile(controlledTerritory);
            if (!controlledTerritory.contains(tile)) {
                specialTiles.add(tile);
                return tile;
            }
        }
        if (specialTiles.size() == 0) {
            int i = 0;
        }
        return randomElement(specialTiles, session.random);
    }

    public void place(ResourcePack resourcePack) {
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