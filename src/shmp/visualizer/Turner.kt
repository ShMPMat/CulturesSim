package shmp.visualizer

import shmp.simulation.Controller
import shmp.simulation.World
import java.util.concurrent.atomic.AtomicBoolean

class Turner(private val turns: Int, private val controller: Controller<out World>) : Runnable {
    @JvmField
    @Volatile
    var isAskedToStop = AtomicBoolean(false)

    private val printTurnDelta = 50

    override fun run() {
        for (i in 0 until turns) {
            controller.turn()
            if (i % printTurnDelta == 0)
                Controller.visualizer.print()

            if (isAskedToStop.get()) {
                println("Terminating Turner thread")
                return
            }
        }
    }
}