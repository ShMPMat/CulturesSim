package shmp.simulation

import shmp.simulation.interactionmodel.InteractionModel
import shmp.simulation.space.SpaceData
import shmp.simulation.space.createRivers
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.tile.Tile
import kotlin.random.Random


class AddRiversInitStep<E : World>(
        private val fillCycles: Int,
        private val doTurns: Boolean,
        private val riversAmount: Int, //(5 * proportionCoefficient * proportionCoefficient).toInt()
        private val stabilizationTurns: Int, //also a flag was present
        private val debugPrint: Boolean,
        private val random: Random
) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        val water = world.resourcePool.getBaseName("Water")
        val riverCreationThreshold = 108

        var j = 0
        while (j < fillCycles && doTurns) {
            createRivers(
                    world.map,
                    riversAmount,
                    water,
                    { t ->
                        if (t.level >= riverCreationThreshold
                                && t.resourcePack.any(::riverResourcePredicate)
                                && t.getTilesInRadius(2) { it.resourcesWithMoved.contains(water) }.isEmpty()
                        )
                            (t.temperature - SpaceData.data.temperatureBaseStart + 1).toDouble() *
                                    (t.level + 1 - riverCreationThreshold)
                        else 0.0
                    },
                    { it.type !== Tile.Type.Ice },
                    random
            )

            if (j != 0)
                world.placeResources()

            if (j != fillCycles - 1 || stabilizationTurns != 0) {
                for (i in 1..stabilizationTurns) {
                    interactionModel.turn(world)
                    if (debugPrint)
                        Controller.visualizer.print()
                }
                interactionModel.turn(world)
            }
            j++
        }

        interactionModel.turn(world)
        world.map.setTags()
    }

    private fun riverResourcePredicate(r: Resource): Boolean =
            r.tags.any { it.name in listOf("liquid", "solid") }
                    && r.genome.materials.any { it.name == "Water" }
}