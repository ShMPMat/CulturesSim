package io.tashtabash.sim

import io.tashtabash.sim.interactionmodel.InteractionModel


class GroupTurnsStep<E : CulturesWorld>(
        private val turnNumber: Int,
        private val debugPrint: Boolean,
) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        world.initializeGroups()
        for (i in 1..turnNumber) {
            interactionModel.turn(world)
            if (debugPrint)
                Controller.visualizer.print()
        }
    }
}