package io.tashtabash.sim.init

import io.tashtabash.sim.World
import io.tashtabash.sim.interactionmodel.InteractionModel
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.generator.condenseMap
import io.tashtabash.sim.space.generator.createTileUpdaters
import io.tashtabash.sim.space.generator.setUpParallelUpdate


class CondenseStep<E : World>(private val factor: Int = data.condensationFactor) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        val fine = world.map
        val map = condenseMap(fine, factor, { createTileUpdaters(world.resourcePool) })
        setUpParallelUpdate(map, world.resourcePool)
        world.map = map

        val splitCells = map.tiles.size - map.maxX * map.maxY
        println(
            "Condensed ${fine.tiles.size} Tiles into ${map.tiles.size} " +
                    "(${fine.maxX}x${fine.maxY} -> ${map.maxX}x${map.maxY}), " +
                    "$splitCells cells are split, " +
                    "map size is ${map.maxX * map.scale.tileSizeKm}x${map.maxY * map.scale.tileSizeKm} km"
        )
    }
}
