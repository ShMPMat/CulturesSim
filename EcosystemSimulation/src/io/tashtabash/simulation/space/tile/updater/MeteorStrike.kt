package io.tashtabash.simulation.space.tile.updater

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.chanceOfNot
import io.tashtabash.simulation.Controller.Companion.session
import io.tashtabash.simulation.event.Type
import io.tashtabash.simulation.event.of
import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.Taker
import io.tashtabash.simulation.space.tile.Tile
import java.lang.Integer.min


class MeteorStrike(private val iron: Resource): TileUpdater {
    override fun update(tile: Tile) {
        0.000001.chanceOfNot {
            return
        }

        val strength = RandomSingleton.random.nextDouble(0.0, 0.9)
        for (resource in tile.resourcePack.resourcesIterator) {
            val deadPart = resource.amount * strength * RandomSingleton.random.nextDouble(0.8, 1.2)

            resource.getPart(min(resource.amount, deadPart.toInt()), Taker.CataclysmTaker)
        }

        val ironAmount = (1000 * strength).toInt()
        tile.addDelayedResource(iron.copy(ironAmount))

        session.world.events.add(Type.Cataclysm of "Meteor of strength $strength strikes tile ${tile.posStr}")
    }
}
