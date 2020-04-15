package simulation.space.resource.dependency;

import simulation.space.tile.Tile;
import simulation.space.resource.Resource;

public interface ResourceDependency {
    double satisfactionPercent(Tile tile, Resource resource);//TODO sometimes null, fix this
    boolean isNecessary();
    boolean isPositive();
    boolean isResourceNeeded();
    boolean hasNeeded(Tile tile);
}
