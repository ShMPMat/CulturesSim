package io.tashtabash.simulation.space.resource.dependency;

import io.tashtabash.simulation.space.tile.Tile;
import io.tashtabash.simulation.space.resource.Resource;

import java.util.Set;


public class AvoidTiles implements ResourceDependency {
    public Set<Tile.Type> badTypes;

    public AvoidTiles(Set<Tile.Type> badTypes) {
        this.badTypes = badTypes;
    }

    @Override
    public double satisfactionPercent(Tile tile, Resource resource) {
        return hasNeeded(tile) ? 1 : 0;
    }

    @Override
    public boolean isNecessary() {
        return true;
    }

    @Override
    public boolean isPositive() {
        return true;
    }

    @Override
    public boolean isResourceNeeded() {
        return false;
    }

    @Override
    public boolean hasNeeded(Tile tile) {
        return !badTypes.contains(tile.getType());
    }
}
