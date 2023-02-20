package shmp.simulation

import shmp.simulation.interactionmodel.InteractionModel


class GeologicalTurnsStep<E : World>(
        private val turnNumber: Int,
        private val debugPrint: Boolean,
) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        for (i in 0 until turnNumber) {
            interactionModel.geologicTurn(world)
            if (debugPrint)
                Controller.visualizer.print()
        }
    }
}
