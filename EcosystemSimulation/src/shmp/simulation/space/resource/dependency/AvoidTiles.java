package shmp.simulation.space.resource.dependency;

import shmp.simulation.space.tile.Tile;
import shmp.simulation.space.resource.Resource;

import java.util.Set;


public class AvoidTiles implements ResourceDependency {
    private Set<Tile.Type> badTypes;

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
