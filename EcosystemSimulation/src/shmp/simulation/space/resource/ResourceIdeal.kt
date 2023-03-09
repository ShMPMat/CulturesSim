package shmp.simulation.space.resource

import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.tile.Tile

/**
 * Special resource instance, which must is never be placed on the map and
 * will print a warning on any attempt of changing it. Can be used as
 * an example instance of a resource inside classes working with resources.
 */
class ResourceIdeal(genome: Genome, amount: Int = 1) : Resource(ResourceCore(genome, ArrayList(), freeMarker), amount) {
    override fun getPart(part: Int, taker: Taker): Resource {
        System.err.println("Ideal is changing")
        return super.getPart(part, taker)
    }

    override fun merge(resource: Resource): Resource {
        System.err.println("Ideal is changing")
        return super.merge(resource)
    }

    override fun update(tile: Tile): ResourceUpdateResult {
        System.err.println("Ideal is changing")
        return super.update(tile)
    }

    override fun addAmount(amount: Int) {
        System.err.println("Ideal is changing")
        super.addAmount(amount)
    }

    override fun applyActionAndConsume(action: ResourceAction, part: Int, isClean: Boolean, taker: Taker): List<Resource> {
        System.err.println("Ideal is changing")
        return super.applyActionAndConsume(action, part, isClean, taker)
    }
}