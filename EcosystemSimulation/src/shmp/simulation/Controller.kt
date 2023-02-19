package shmp.simulation

import shmp.random.singleton.RandomSingleton
import shmp.simulation.interactionmodel.InteractionModel
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

    protected val initSteps = mutableListOf<ControllerInitStep<E>>(
            AddRiversInitStep(
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
