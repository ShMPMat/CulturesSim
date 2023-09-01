package io.tashtabash.visualizer

import io.tashtabash.sim.Controller
import io.tashtabash.sim.World
import java.util.concurrent.atomic.AtomicBoolean


class Turner(
        private val turns: Int,
        private val printTurnStep: Int,
        private val controller: Controller<out World>
) : Runnable {
    @JvmField
    @Volatile
    var isAskedToStop = AtomicBoolean(false)

    override fun run() {
        for (i in 0 until turns) {
            controller.turn()
            if (i % printTurnStep == 0)
                Controller.visualizer.print()

            if (isAskedToStop.get()) {
                println("Terminating Turner thread")
                return
            }
        }
    }
}
