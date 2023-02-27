package shmp.simulation

import shmp.simulation.interactionmodel.InteractionModel


class GroupTurnsStep<E : CulturesWorld>(
        private val turnNumber: Int,
        private val debugPrint: Boolean,
) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        world.initializeGroups()
        var i = 0
        while (i < turnNumber) {//TODO range
            interactionModel.turn(world)
            if (debugPrint)
                Controller.visualizer.print()
            i++
        }
    }
}
