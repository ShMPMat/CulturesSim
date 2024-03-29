package io.tashtabash.sim

import io.tashtabash.sim.interactionmodel.InteractionModel


class GeologicalTurnsStep<E : World>(
        private val turnNumber: Int,
        private val debugPrint: Boolean,
) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        for (i in 1..turnNumber) {
            interactionModel.geologicTurn(world)
            if (debugPrint)
                Controller.visualizer.print()
        }
    }
}
