package shmp.simulation

import shmp.random.singleton.RandomSingleton
import shmp.simulation.interactionmodel.InteractionModel
import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.createRivers
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.tile.Tile
import shmp.visualizer.Visualizer
import kotlin.random.Random


open class Controller<E : World>(val interactionModel: InteractionModel<E>, val world: E) {
    val random = Random(8565728 + 38)

    val doTurns = true

    val geologyTurns = 50
    val initialTurns = 100
    val stabilizationTurns = 100
    val fillCycles = 3

    val proportionCoefficient = 1.0

    private val debugPrint = false
    private val doLastStabilization = true

    init {
        session = this
        RandomSingleton.safeRandom = random
    }

    fun initializeFirst() {
        for (i in 0 until geologyTurns) {
            interactionModel.geologicTurn(world)
            if (debugPrint)
                visualizer.print()
        }
        for (i in 0 until initialTurns) {
            turn()
            if (debugPrint)
                visualizer.print()
        }
        world.placeResources()
    }

    fun initializeSecond() {
        fun riverResourcePredicate(r: Resource) =
                (r.tags.any { it.name in listOf("liquid", "solid") }
                        && r.genome.materials.any { it.name == "Water" })

        val water = world.resourcePool.getBaseName("Water")
        val riverCreationThreshold = 108
        var j = 0
        while (j < fillCycles && doTurns) {
            createRivers(
                    world.map,
                    (5 * proportionCoefficient * proportionCoefficient).toInt(),
                    water,
                    { t ->
                        if (t.level >= riverCreationThreshold
                                && t.resourcePack.any(::riverResourcePredicate)
                                && t.getTilesInRadius(2) { it.resourcesWithMoved.contains(water) }.isEmpty()
                        )
                            (t.temperature - data.temperatureBaseStart + 1).toDouble() *
                                    (t.level + 1 - riverCreationThreshold)
                        else 0.0
                    },
                    { it.type !== Tile.Type.Ice },
                    random
            )
            if (j != 0)
                world.placeResources()
            if (j != fillCycles - 1 || doLastStabilization) {
                for (i in 0 until stabilizationTurns) {
                    turn()
                    if (debugPrint)
                        visualizer.print()
                }
                turn()
            }
            j++
        }
        turn()
        world.map.setTags()
    }

    fun isTime(denominator: Int) = world.lesserTurnNumber % denominator == 0

    open fun turn() {
        interactionModel.turn(world)
    }

    open fun geologicTurn() {
        interactionModel.geologicTurn(world)
    }

    companion object {
        lateinit var session: Controller<*>
        lateinit var visualizer: Visualizer
    }
}
