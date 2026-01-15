package io.tashtabash.sim.init

import io.tashtabash.sim.Controller
import io.tashtabash.sim.World
import io.tashtabash.sim.interactionmodel.InteractionModel


class GeologicalTurnsStep<E : World>(
        private val turnNumber: Int,
        private val debugPrint: Boolean,
) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        repeat(turnNumber) {
            interactionModel.geologicTurn(world)
            if (debugPrint)
                Controller.visualizer.print()
        }
    }
}
