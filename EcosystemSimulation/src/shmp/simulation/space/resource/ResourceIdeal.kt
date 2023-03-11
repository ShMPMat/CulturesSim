package shmp.simulation.space.resource

import shmp.simulation.space.tile.Tile
import javax.naming.OperationNotSupportedException

/**
 * Special resource instance, which must is never be placed on the map and
 * will print a warning on any attempt of changing it. Can be used as
 * an example instance of a resource inside classes working with resources.
 */
class ResourceIdeal(genome: Genome, amount: Int = 1) : Resource(ResourceCore(genome, ArrayList(), freeMarker), amount) {
    override fun getPart(part: Int, taker: Taker): Resource {
        throw OperationNotSupportedException("ResourceIdeal doesn't support getPart(..)")
    }

    override fun getCleanPart(part: Int, taker: Taker): Resource {
        throw OperationNotSupportedException("ResourceIdeal doesn't support applyActionAndConsume(..)")
    }

    override fun merge(resource: Resource): Resource {
        throw OperationNotSupportedException("ResourceIdeal doesn't support merge(..)")
    }

    override fun update(tile: Tile): ResourceUpdateResult {
        throw OperationNotSupportedException("ResourceIdeal doesn't support update(..)")
    }

    override fun addAmount(amount: Int) {
        throw OperationNotSupportedException("ResourceIdeal doesn't support addAmount(..)")
    }
}