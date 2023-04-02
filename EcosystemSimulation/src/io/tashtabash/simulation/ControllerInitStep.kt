package io.tashtabash.simulation

import io.tashtabash.simulation.interactionmodel.InteractionModel


interface ControllerInitStep<E: World> {
    fun run(world: E, interactionModel: InteractionModel<E>)
}
