package shmp.visualizer

import shmp.simulation.space.resource.container.ResourcePool
import shmp.simulation.space.tile.Tile
import java.util.*


fun addResourceOnTile(tile: Tile?, resourceName: String, resourcePool: ResourcePool) {
    if (tile == null) {
        System.err.println("No such Tile")
        return
    }
    try {
        val resource = resourcePool.getBaseName(resourceName)
        tile.addDelayedResource(resource.copy())
    } catch (e: NoSuchElementException) {
        System.err.println("No such Resource")
    }
}
