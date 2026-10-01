package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.sim.space.resource.freeMarker
import kotlin.math.max
import kotlin.math.min


class WindCenter internal constructor() {
    var wind = Wind()
        internal set

    private var _newWind = Wind()

    fun useWind(resources: List<Resource>) {
        _newWind = Wind()
        if (wind.isStill)
            return

        for (resource in resources) {
            // Take the amount before the loop, so that the share doesn't depend on the order of affectedTiles
            val amount = resource.amount
            // The strongest direction defines how much is blown away, the rest is split
            val blownShare = min(1.0, wind.maxLevel / data.maxWind * getFlyCoefficient(resource))
            for ((tile, level) in wind.affectedTiles) {
                val part = (amount * blownShare * level / wind.sumLevel).toInt()

                if (part > 0)
                    tile.addDelayedResource(resource.getCleanPart(part, Taker.WindTaker))
            }
        }
    }

    private fun getFlyCoefficient(resource: Resource) = .00001 /
            resource.genome.mass /
            (if (resource.core.ownershipMarker == freeMarker) 1 else 10)

    fun middleUpdate(x: Int, y: Int, map: WorldMap) {
        val host = map[x, y]
            ?: return

        host.neighbours.forEach { setWindByTemperature(it, host) }

        propagateWindStraight(map[x - 1, y], map[x + 1, y], host)
        propagateWindStraight(map[x + 1, y], map[x - 1, y], host)
        propagateWindStraight(map[x, y - 1], map[x, y + 1], host)
        propagateWindStraight(map[x, y + 1], map[x, y - 1], host)

        map[x, y - 1]?.let {
            _newWind.changeLevelOnTile(it, data.coriolisEffect)
        }
    }

    fun finishUpdate() {
        wind = _newWind
    }

    private fun setWindByTemperature(tile: Tile?, master: Tile) {
        tile ?: return

        var change = data.temperatureToWind
        if (tile.level + 2 < master.level)
            change *= 5
        if (tile.type == master.type)
            change *= 5

        val level = max(tile.temperature - master.temperature, 0.0) / change
        if (level > 0)
            _newWind.changeLevelOnTile(tile, level)
    }

    private fun propagateWindStraight(target: Tile?, tile: Tile?, master: Tile) {
        tile ?: return
        target ?: return

        val level = tile.wind.getLevelByTile(master) * (1 - data.windFriction) - data.windPropagationDrag
        if (level > 0)
            _newWind.changeLevelOnTile(target, level)
    }
}
