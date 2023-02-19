package shmp.simulation

import shmp.simulation.interactionmodel.InteractionModel


interface ControllerInitStep<E: World> {
    fun run(world: E, interactionModel: InteractionModel<E>)
}
