package io.tashtabash.sim.space.tile.updater

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.sim.space.tile.Direction
import io.tashtabash.sim.space.tile.Tile


class FlowTransferUpdater(val water: Resource): TileUpdater {
    override fun update(tile: Tile) {
        if (tile.flow.strength == 0.0)
            return

        val flow = tile.flow

        for (resource in tile.resourcePack.resourcesIterator) {
            val speedDiff = flow.strength - resource.genome.behaviour.tileSpeed(tile.scale)

            if (!resource.genome.isMovable && speedDiff > 0)
                continue

            val overallPart = (resource.amount * (1 - 1 / (speedDiff + 1))).toInt()

            if (overallPart > 0) {
                if (flow.x > 0)
                    moveResources(tile, Direction.XPlus, resource, overallPart, flow.x / flow.strength)
                if (flow.x < 0)
                    moveResources(tile, Direction.XMinus, resource, overallPart, -flow.x / flow.strength)
                if (flow.y > 0)
                    moveResources(tile, Direction.YPlus, resource, overallPart, flow.y / flow.strength)
                if (flow.y < 0)
                    moveResources(tile, Direction.YMinus, resource, overallPart, -flow.y / flow.strength)
            }
        }
    }

    private fun moveResources(from: Tile, direction: Direction, resource: Resource, part: Int, flowPart: Double) {
        val neighboursCount = from.countNeighboursIn(direction)
        if (neighboursCount == 0)
            return

        val neighbourPart = (part * flowPart).toInt() / neighboursCount
        from.forEachNeighbourIn(direction) { //Split evenly
            it.addDelayedResource(resource.getCleanPart(neighbourPart, Taker.FlowTaker))
        }
    }
}
