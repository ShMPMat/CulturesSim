package io.tashtabash.sim

import io.tashtabash.sim.interactionmodel.InteractionModel
import io.tashtabash.sim.space.SpaceData
import io.tashtabash.sim.space.createRivers
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.tile.Tile
import kotlin.random.Random


class AddRiversInitStep<E : World>(
        private val fillCycles: Int,
        private val doTurns: Boolean,
        private val riversAmount: Int,
        private val stabilizationTurns: Int,
        private val debugPrint: Boolean,
        private val random: Random
) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        val water = world.resourcePool.getBaseName("Water")
        val riverCreationThreshold = 108

        var i = 0
        while (i < fillCycles && doTurns) {
            createRivers(
                    world.map,
                    riversAmount,
                    water,
                    { t ->
                        if (t.level >= riverCreationThreshold
                                && t.resourcePack.any(::riverResourcePredicate)
                                && t.getTilesInRadius(2) { it.resourcesWithMoved.contains(water) }.isEmpty()
                        )
                            (t.temperature - SpaceData.data.temperatureBaseStart + 1) *
                                    (t.level + 1 - riverCreationThreshold)
                        else 0.0
                    },
                    { it.type !== Tile.Type.Ice },
                    random
            )

            if (i != 0)
                world.placeResources()

            if (i != fillCycles - 1 || stabilizationTurns != 0) {
                for (j in 1..stabilizationTurns) {
                    interactionModel.turn(world)
                    if (debugPrint)
                        Controller.visualizer.print()
                }
                interactionModel.turn(world)
            }
            i++
        }

        interactionModel.turn(world)
        world.map.setTags()
    }

    private fun riverResourcePredicate(r: Resource): Boolean =
            r.tags.any { it.name in listOf("liquid", "solid") }
                    && r.genome.materials.any { it.name == "Water" }
}
