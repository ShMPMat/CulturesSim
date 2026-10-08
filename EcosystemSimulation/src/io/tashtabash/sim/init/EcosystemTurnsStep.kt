package io.tashtabash.sim.init

import io.tashtabash.sim.Controller
import io.tashtabash.sim.World
import io.tashtabash.sim.interactionmodel.InteractionModel


class EcosystemTurnsStep<E : World>(private val turns: Int, private val debugPrint: Boolean) : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        repeat(turns) {
            interactionModel.turn(world)
            if (debugPrint)
                Controller.visualizer.print()
        }
        world.placeResources()
    }
}
