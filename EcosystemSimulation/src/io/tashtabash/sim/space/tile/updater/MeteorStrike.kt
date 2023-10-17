package io.tashtabash.sim.space.tile.updater

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.chanceOfNot
import io.tashtabash.sim.Controller.Companion.session
import io.tashtabash.sim.event.Cataclysm
import io.tashtabash.sim.event.Type
import io.tashtabash.sim.event.of
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.sim.space.tile.Tile
import java.lang.Integer.min


class MeteorStrike(private val iron: Resource): TileUpdater {
    override fun update(tile: Tile) {
        0.000001.chanceOfNot {
            return
        }

        val strength = RandomSingleton.random.nextDouble(0.0, 5 * session.proportionCoefficient)
        val oldTiles = mutableSetOf<Tile>()
        var currentStrength = strength
        var currentTiles = listOf(tile)
        while (currentStrength > 0) {
            for (currentTile in currentTiles)
                destroyTileResources(currentTile, currentStrength)

            currentStrength -= 1
            oldTiles += currentTiles
            currentTiles = currentTiles.flatMap { it.neighbours } - oldTiles
        }

        val ironAmount = (1000 * strength).toInt()
        tile.addDelayedResource(iron.copy(ironAmount))

        session.world.events.add(Cataclysm of "Meteor of strength $strength strikes tile ${tile.posStr}")
    }

    private fun destroyTileResources(tile: Tile, strength: Double) {
        for (resource in tile.resourcesWithMoved) {
            val deadPart = resource.amount * strength * RandomSingleton.random.nextDouble(0.8, 1.2)

            resource.getCleanPart(min(resource.amount, deadPart.toInt()), Taker.CataclysmTaker)
        }
    }
}
