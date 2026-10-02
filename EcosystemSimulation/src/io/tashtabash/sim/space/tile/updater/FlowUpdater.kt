package io.tashtabash.sim.space.tile.updater

import io.tashtabash.sim.space.tile.Direction
import io.tashtabash.sim.space.tile.Tile
import kotlin.math.abs


class FlowUpdater : TileUpdater {
    var updatedFlowX = 0.0
    var updatedFlowY = 0.0

    private var windEffectX = 0.0
    private var windEffectY = 0.0
    private val windDecreaseCoefficient = 2

    override fun update(tile: Tile) {
        if (tile.type != Tile.Type.Water)
            return

        tile.flow.x = updatedFlowX
        tile.flow.y = updatedFlowY

        windEffectX = 0.0
        windEffectY = 0.0
        for ((affectedTile, strength) in tile.wind.affectedTiles)
            when (tile.directionOf(affectedTile)) {
                Direction.XPlus -> windEffectX += strength / windDecreaseCoefficient
                Direction.XMinus -> windEffectX -= strength / windDecreaseCoefficient
                Direction.YPlus -> windEffectY += strength / windDecreaseCoefficient
                Direction.YMinus -> windEffectY -= strength / windDecreaseCoefficient
                Direction.Here, null -> {}
            }

        // Take the flow coming towards this Tile
        tile.forEachNeighbourIn(Direction.XPlus) { propagateFlow(it.flow.x.coerceAtMost(0.0), 0.0) }
        tile.forEachNeighbourIn(Direction.XMinus) { propagateFlow(it.flow.x.coerceAtLeast(0.0), 0.0) }
        tile.forEachNeighbourIn(Direction.YPlus) { propagateFlow(0.0, it.flow.y.coerceAtMost(0.0)) }
        tile.forEachNeighbourIn(Direction.YMinus) { propagateFlow(0.0, it.flow.y.coerceAtLeast(0.0)) }

        divertFlow(isWaterIn(tile, Direction.XPlus), 1, 0)
        divertFlow(isWaterIn(tile, Direction.XMinus), -1, 0)
        divertFlow(isWaterIn(tile, Direction.YPlus), 0, 1)
        divertFlow(isWaterIn(tile, Direction.YMinus), 0, -1)


        if (abs(updatedFlowX) + abs(updatedFlowY) < abs(windEffectX))
            updatedFlowX += windEffectX
        if (abs(updatedFlowY) + abs(updatedFlowX) < abs(windEffectY))
            updatedFlowY += windEffectY
    }

    private fun propagateFlow(x: Double, y: Double) {
        if (abs(updatedFlowX) + abs(updatedFlowY) < abs(x) + abs(y)) {
            updatedFlowX = x
            updatedFlowY = y
        }
    }

    private fun isWaterIn(tile: Tile, direction: Direction) =
        tile.anyNeighbourIn(direction) { it.type == Tile.Type.Water }

    // Turns the flow aside if it runs into a shore or the map edge
    private fun divertFlow(isWaterAhead: Boolean, xShift: Int, yShift: Int) {
        if (isWaterAhead)
            return

        if (updatedFlowX > 0 && xShift > 0) {
            updatedFlowX /= 4
            updatedFlowY -= updatedFlowX * 3
        } else if (updatedFlowX < 0 && xShift < 0) {
            updatedFlowX /= 4
            updatedFlowY -= updatedFlowX * 3
        } else if (updatedFlowY > 0 && yShift > 0) {
            updatedFlowY /= 4
            updatedFlowX += updatedFlowY * 3
        } else if (updatedFlowY < 0 && yShift < 0) {
            updatedFlowY /= 4
            updatedFlowX += updatedFlowY * 3
        }
    }
}
