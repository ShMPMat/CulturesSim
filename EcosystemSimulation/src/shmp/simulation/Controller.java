package shmp.simulation;

import kotlin.random.Random;
import kotlin.random.RandomKt;
import shmp.random.singleton.RandomSingleton;
import shmp.simulation.interactionmodel.InteractionModel;
import shmp.simulation.space.LandscapeChangesKt;
import shmp.simulation.space.SpaceData;
import shmp.simulation.space.resource.Resource;
import shmp.simulation.space.tile.Tile;
import shmp.visualizer.Visualizer;


public class Controller<E extends World> {
    public static Controller<?> session;
    public E world;
    public InteractionModel<E> interactionModel;

    public final Random random = RandomKt.Random(8565728 + 36);

    public final boolean doTurns = true;

    public final int geologyTurns = 50;
    public final int initialTurns = 100;
    public final int stabilizationTurns = 100;
    public final int fillCycles = 3;

    public final double proportionCoefficient = 1.5;

    public static Visualizer visualizer;
    private final boolean debugPrint = false;
    private final boolean doLastStabilization = true;

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
            if (debugPrint) {
                visualizer.print();
            }
        }
        for (int i = 0; i < initialTurns; i++) {
            turn();
            if (debugPrint) {
                visualizer.print();
            }
        }
        world.placeResources();
    }

    public void initializeSecond() {
        Resource water = world.getResourcePool().getBaseName("Water");
        int riverCreationThreshold = 108;
        for (int j = 0; j < fillCycles && doTurns; j++) {
            LandscapeChangesKt.createRivers(
                    world.getMap(),
                    (int) (5 * proportionCoefficient * proportionCoefficient),
                    water,
                    t -> t.getLevel() >= riverCreationThreshold
                            && t.getResourcePack().any(r ->
                            r.getTags().stream().anyMatch(tag -> tag.getName().equals("liquid") || tag.getName().equals("solid"))
                                    && r.getGenome().getMaterials().stream().anyMatch(m -> m.getName().equals("Water"))
                    )
                            && t.getTilesInRadius(2, n -> n.getResourcePack().contains(water)).isEmpty()
                            ? (t.getTemperature() - SpaceData.INSTANCE.getData().getTemperatureBaseStart() + 1) * (t.getLevel() + 1 - riverCreationThreshold) : 0.0,
                    t -> t.getType() != Tile.Type.Ice,
                    random
            );
            if (j != 0) {
                world.placeResources();
            }

            if (j != fillCycles - 1 || doLastStabilization) {
                for (int i = 0; i < stabilizationTurns; i++) {
                    turn();
                    if (debugPrint) {
                        visualizer.print();
                    }
                }
                turn();
            }
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
