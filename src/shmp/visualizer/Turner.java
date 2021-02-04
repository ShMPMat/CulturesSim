package shmp.visualizer;

import shmp.simulation.Controller;

import java.util.concurrent.atomic.AtomicBoolean;


public class Turner implements Runnable {
    public volatile AtomicBoolean isAskedToStop = new AtomicBoolean(false);
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
                Controller.visualizer.print();
            }
            if (isAskedToStop.get()) {
                System.out.println("Terminating Turner thread");
                return;
            }
        }
    }
}
