package shmp.simulation;

import kotlin.random.Random;
import kotlin.random.RandomKt;
import shmp.random.singleton.RandomSingleton;
import shmp.simulation.interactionmodel.InteractionModel;
import shmp.simulation.space.LandscapeChangesKt;
import shmp.simulation.space.resource.Resource;
import shmp.simulation.space.tile.Tile;
import shmp.visualizer.Visualizer;


public class Controller<E extends World> {
    public static Controller session;
    public E world;
    public InteractionModel<E> interactionModel;

    public final Random random = RandomKt.Random(8565728 + 8);

    public final boolean doTurns = true;

    public final int geologyTurns = 50;
    public final int initialTurns = 100;
    public final int stabilizationTurns = 100;
    public final int fillCycles = 2;
    public final int cultureTurns = 0;

    public final int proportionCoefficient = 1;

    public static Visualizer visualizer;
    public static final boolean doPrint = false;

    public long overallTime = 0;
    public long groupTime = 0;
    public long othersTime = 0;
    public long groupMainTime = 0;
    public long groupOthersTime = 0;
    public long groupMigrationTime = 0;
    public long groupInnerOtherTime = 0;

    public Controller(InteractionModel<E> interactionModel) {
        session = this;
        RandomSingleton.INSTANCE.setSafeRandom(random);

        this.interactionModel = interactionModel;
    }

    public void initializeWorld(E world) {
        this.world = world;
    }

    public void initializeFirst() {
        for (int i = 0; i < geologyTurns; i++) {
            geologicTurn();
            if (doPrint) {
                visualizer.print();
            }
        }
        for (int i = 0; i < initialTurns; i++) {
            turn();
            if (doPrint) {
                visualizer.print();
            }
        }
        world.fillResources();
    }

    public void initializeSecond() {
        Resource water = world.getResourcePool().getBaseName("Water");
        for (int j = 0; j < fillCycles && doTurns; j++) {
            if (j != 0) {
                world.fillResources();
            }
            for (int i = 0; i < stabilizationTurns; i++) {
                turn();
                if (doPrint) {
                    visualizer.print();
                }
            }
            LandscapeChangesKt.createRivers(
                    world.getMap(),
                    5 * proportionCoefficient * proportionCoefficient,
                    water,
                    t -> t.getType() == Tile.Type.Mountain
                            && t.getResourcePack().contains(world.getResourcePool().getBaseName("Snow"))
                            && t.getTilesInRadius(2, n -> n.getResourcePack().contains(water)).isEmpty()
                            ? (t.getTemperature() + 100) * (t.getTemperature() + 100) : 0.0,
                    t -> t.getType() != Tile.Type.Ice,
                    random
            );
            turn();
        }
        turn();
        world.getMap().setTags();
    }

    public boolean isTime(int denominator) {
        return world.getLesserTurnNumber() % denominator == 0;
    }

    public void turn() {
        interactionModel.turn(world);
        world.incrementTurn();
    }

    public void geologicTurn() {
        interactionModel.geologicTurn(world);
        world.incrementTurnGeology();
    }
}
