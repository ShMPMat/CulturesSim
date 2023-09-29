package io.tashtabash.sim.space.tile.updater

import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.tile.Tile
import kotlin.math.abs


class FlowUpdater(val map: WorldMap): TileUpdater {
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
        for ((affectedTile, strength) in tile.wind.affectedTiles) {
            if (affectedTile == map[tile.x + 1, tile.y])
                windEffectX += strength / windDecreaseCoefficient
            if (affectedTile == map[tile.x - 1, tile.y])
                windEffectX -= strength / windDecreaseCoefficient
            if (affectedTile == map[tile.x, tile.y + 1])
                windEffectY += strength / windDecreaseCoefficient
            if (affectedTile == map[tile.x, tile.y - 1])
                windEffectY -= strength / windDecreaseCoefficient
        }

        propagateFlow(map[tile.x + 1, tile.y]?.flow?.x?.coerceAtMost(0.0) ?: 0.0, 0.0)
        propagateFlow(map[tile.x - 1, tile.y]?.flow?.x?.coerceAtLeast(0.0)  ?: 0.0, 0.0)
        propagateFlow(0.0, map[tile.x, tile.y + 1]?.flow?.y?.coerceAtMost(0.0) ?: 0.0)
        propagateFlow(0.0, map[tile.x, tile.y - 1]?.flow?.y?.coerceAtLeast(0.0)  ?: 0.0)

        divertFlow(map[tile.x + 1, tile.y], 1, 0)
        divertFlow(map[tile.x - 1, tile.y], -1, 0)
        divertFlow(map[tile.x, tile.y + 1], 0, 1)
        divertFlow(map[tile.x, tile.y - 1], 0, -1)


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

    private fun divertFlow(nextTile: Tile?, xShift: Int, yShift: Int) {
        if (nextTile != null && nextTile.type == Tile.Type.Water)
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
