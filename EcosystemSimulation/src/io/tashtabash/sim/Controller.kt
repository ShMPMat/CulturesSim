package io.tashtabash.sim

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.init.AddRiversStep
import io.tashtabash.sim.init.CondenseStep
import io.tashtabash.sim.init.ControllerInitStep
import io.tashtabash.sim.init.EcosystemTurnsStep
import io.tashtabash.sim.init.GeologicalTurnsStep
import io.tashtabash.sim.interactionmodel.InteractionModel
import io.tashtabash.sim.space.SpaceData
import io.tashtabash.visualizer.Visualizer
import kotlin.random.Random
import kotlin.time.measureTime


open class Controller<E : World>(val interactionModel: InteractionModel<E>, val world: E, val proportionCoefficient: Double) {
    val random = Random(8565728 + 38)

    val doTurns = true

    val geologyTurns = 50
    val initialTurns = 100
    val stabilizationTurns = 100
    val fillCycles = 3

    private val debugPrint = false

    protected val initSteps = mutableListOf<ControllerInitStep<E>>(
        GeologicalTurnsStep(geologyTurns, debugPrint),
        CondenseStep(),
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
            println("Run init step ${initStep.javaClass.simpleName}")
            val time = measureTime {
                initStep.run(world, interactionModel)
            }
            println("Finished init step ${initStep.javaClass.simpleName} in: $time\n")
        }
    }

    fun isTime(denominator: Int) = (world.day / SpaceData.data.tickDurationDays)  % denominator == 0L

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
