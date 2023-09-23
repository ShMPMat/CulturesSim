package io.tashtabash.sim.space.tile.updater

import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.sim.space.tile.Tile


class FlowTransferUpdater(val map: WorldMap, val water: Resource): TileUpdater {
    override fun update(tile: Tile) {
        if (tile.flow.strength == 0.0)
            return

        val flow = tile.flow

        for (resource in tile.resourcePack.resourcesIterator) {
            val speedDiff = flow.strength - resource.genome.behaviour.speed

            if (!resource.genome.isMovable && speedDiff > 0)
                continue

            val overallPart = (resource.amount * (1 - 1 / (speedDiff + 1))).toInt()

            if (overallPart > 0) {
                if (flow.x > 0)
                    moveResources(map[tile.x + 1, tile.y], resource, overallPart, flow.x / flow.strength)
                if (flow.x < 0)
                    moveResources(map[tile.x - 1, tile.y], resource, overallPart, -flow.x / flow.strength)
                if (flow.y > 0)
                    moveResources(map[tile.x, tile.y + 1], resource, overallPart, flow.y / flow.strength)
                if (flow.y < 0)
                    moveResources(map[tile.x, tile.y - 1], resource, overallPart, -flow.y / flow.strength)
            }
        }
    }

    private fun moveResources(tile: Tile?, resource: Resource, part: Int, flowPart: Double) {
        tile?.addDelayedResource(resource.getCleanPart((part * flowPart).toInt(), Taker.FlowTaker))
    }
}
