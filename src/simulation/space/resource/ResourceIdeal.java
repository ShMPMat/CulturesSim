package simulation.space.resource;

import simulation.culture.aspect.Aspect;
import simulation.space.tile.Tile;

import java.util.List;

/**
 * Special resource instance, which is never placed on the map and prints
 * a warning on any attempt of changing it, but which can be used as
 * an example instance of a resource inside classes working with resources.
 */
public class ResourceIdeal extends Resource {
    public ResourceIdeal(ResourceCore resourceCore) {
        super(resourceCore, 1);
    }

    @Override
    public Resource getPart(int part) {
        System.err.println("Ideal is changing");
        return super.getPart(part);
    }

    @Override
    public Resource merge(Resource resource) {
        System.err.println("Ideal is changing");
        return super.merge(resource);
    }

    @Override
    public ResourceUpdateResult update(Tile tile) {
        System.err.println("Ideal is changing");
        return super.update(tile);
    }

    @Override
    public void addAmount(int amount) {
        System.err.println("Ideal is changing");
        super.addAmount(amount);
    }

    @Override
    public List<Resource> applyAndConsumeAspect(Aspect aspect, int part, boolean isClean) {
        System.err.println("Ideal is changing");
        return super.applyAndConsumeAspect(aspect, part, isClean);
    }
}
