package simulation.space.resource.dependency;

import simulation.space.Tile;
import simulation.space.resource.Resource;

public interface ResourceDependency {
    double satisfactionPercent(Tile tile, Resource resource);
    boolean isNecessary();
    boolean isPositive();
    boolean isResourceNeeded();
    boolean hasNeeded(Tile tile);
}
