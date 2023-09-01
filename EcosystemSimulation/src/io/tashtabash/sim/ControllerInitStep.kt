package io.tashtabash.sim

import io.tashtabash.sim.interactionmodel.InteractionModel


interface ControllerInitStep<E: World> {
    fun run(world: E, interactionModel: InteractionModel<E>)
}
