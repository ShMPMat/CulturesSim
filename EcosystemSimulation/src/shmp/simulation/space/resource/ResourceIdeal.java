package shmp.simulation.space.resource;

import shmp.simulation.space.resource.action.ResourceAction;
import shmp.simulation.space.tile.Tile;

import java.util.ArrayList;
import java.util.List;

import static shmp.simulation.space.resource.OwnershipMarkerKt.getFreeMarker;

/**
 * Special resource instance, which is never placed on the map and prints
 * a warning on any attempt of changing it, but which can be used as
 * an example instance of a resource inside classes working with resources.
 */
public class ResourceIdeal extends Resource {
    public ResourceIdeal(Genome genome) {
        super(new ResourceCore(genome, new ArrayList<>(), getFreeMarker()), 1);
    }

    public ResourceIdeal(Genome genome, int amount) {
        super(new ResourceCore(genome, new ArrayList<>(), getFreeMarker()), amount);
    }

    @Override
    public Resource getPart(int part, Taker taker) {
        System.err.println("Ideal is changing");
        return super.getPart(part, taker);
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
    public List<Resource> applyActionAndConsume(ResourceAction action, int part, boolean isClean, Taker taker) {
        System.err.println("Ideal is changing");
        return super.applyActionAndConsume(action, part, isClean, taker);
    }
}
