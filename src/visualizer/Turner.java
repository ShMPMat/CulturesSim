package visualizer;

import simulation.Controller;

import java.util.concurrent.atomic.AtomicBoolean;

public class Turner implements Runnable {
    volatile AtomicBoolean isAskedToStop = new AtomicBoolean(false);
    private int turns;
    private Controller controller;
    private int printTurnDelta = 50;

    public Turner(int turns, Controller controller) {
        this.turns = turns;
        this.controller = controller;
    }

    @Override
    public void run() {
        for (int i = 0; i < turns; i++) {
            controller.turn();
            if (i % printTurnDelta == 0) {
                System.out.println(controller.world.getTurn());
            }
            if (isAskedToStop.get()) {
                System.out.println("Terminating Turner thread");
                return;
            }
        }
    }
}
