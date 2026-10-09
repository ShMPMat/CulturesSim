package io.tashtabash.sim.init

import io.tashtabash.sim.World
import io.tashtabash.sim.interactionmodel.InteractionModel
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.generator.LandscapeData
import io.tashtabash.sim.space.generator.condenseMap
import io.tashtabash.sim.space.generator.createTileUpdaters
import io.tashtabash.sim.space.generator.setUpParallelUpdate


class CondenseStep<E : World>(private val factor: Int = data.condensationFactor) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        val oldSize = world.map.tiles.size
        world.stripBaseMap()
        val map = condenseMap(// Use `map` in order to preserve SaltWater
            LandscapeData(world.map.coveredRegion, world.map.scale, world.map.extent, world.map::getTilesAt),
            factor,
            { createTileUpdaters(world.resourcePool) }
        )
        setUpParallelUpdate(map, world.resourcePool)
        world.map = map

        val splitCells = map.tiles.size - map.coveredRegion.sizeX * map.coveredRegion.sizeY
        val scale = "${map.coveredRegion.sizeX * map.scale.tileSizeKm}x${map.coveredRegion.sizeY * map.scale.tileSizeKm}"
        println(
            "Condensed $oldSize Tiles into ${map.tiles.size} " +
                    "(${world.baseMap.coveredRegion} -> ${map.coveredRegion}), " +
                    "$splitCells cells are split, map size is $scale km"
        )
    }
}
