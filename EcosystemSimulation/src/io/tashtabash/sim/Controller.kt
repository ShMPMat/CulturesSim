package io.tashtabash.sim

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.init.AddRiversStep
import io.tashtabash.sim.init.ControllerInitStep
import io.tashtabash.sim.init.EcosystemTurnsStep
import io.tashtabash.sim.init.GeologicalTurnsStep
import io.tashtabash.sim.interactionmodel.InteractionModel
import io.tashtabash.visualizer.Visualizer
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

    protected val initSteps = mutableListOf<ControllerInitStep<E>>(
        GeologicalTurnsStep(geologyTurns, debugPrint),
        EcosystemTurnsStep(initialTurns, debugPrint),
        AddRiversStep(
            fillCycles,
            doTurns,
            (5 * proportionCoefficient * proportionCoefficient).toInt(),
            stabilizationTurns,
            debugPrint,
            random
        )
    )

    init {
        session = this
        RandomSingleton.safeRandom = random
    }

    fun runInitSteps() {
        for (initStep in initSteps) {
            initStep.run(world, interactionModel)
            println()
        }
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
