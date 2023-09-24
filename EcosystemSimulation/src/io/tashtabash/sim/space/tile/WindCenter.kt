package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.sim.space.resource.freeMarker
import kotlin.math.max
import kotlin.math.pow


class WindCenter internal constructor() {
    var wind = Wind()
        private set

    private var _newWind = Wind()

    fun startUpdate() {
        _newWind = Wind()
    }

    fun useWind(resources: List<Resource>) {
        for (resource in resources) {
            if (!resource.genome.isMovable)
                continue

            for ((tile, t) in wind.affectedTiles) {
                val part = (resource.amount * t.pow(1) / wind.sumLevel * getFlyCoefficient(resource)).toInt()

                if (part > 0)
                    tile.addDelayedResource(resource.getCleanPart(part, Taker.WindTaker))
            }
        }
    }

    private fun getFlyCoefficient(resource: Resource) = 0.0001 /
            resource.genome.mass /
            if (resource.core.ownershipMarker == freeMarker) 1 else 10

    fun middleUpdate(x: Int, y: Int, map: WorldMap) {
        val host = map[x, y]
                ?: return

        host.neighbours.forEach { setWindByTemperature(it, host) }

        if (!_newWind.isStill)
            return

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

        var change = data.temperatureToWindCoefficient
        if (tile.level + 2 < master.level)
            change *= 5
        if (tile.type == master.type)
            change *= 5

        val level = max(tile.temperature - 1 - master.temperature, 0.0) / change
        if (level > 0)
            _newWind.changeLevelOnTile(tile, level)
    }

    private fun propagateWindStraight(target: Tile?, tile: Tile?, master: Tile) {
        tile ?: return
        target ?: return

        val level = tile.wind.getLevelByTile(master) - data.windPropagation
        if (level > 0)
            _newWind.changeLevelOnTile(target, level)
    }
}
