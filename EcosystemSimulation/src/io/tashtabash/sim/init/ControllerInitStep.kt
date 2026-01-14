package io.tashtabash.sim.init

import io.tashtabash.sim.World
import io.tashtabash.sim.interactionmodel.InteractionModel


interface ControllerInitStep<E: World> {
    fun run(world: E, interactionModel: InteractionModel<E>)
}
