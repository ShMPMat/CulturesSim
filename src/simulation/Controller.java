package simulation;

import simulation.culture.interactionmodel.InteractionModel;

/**
 * Class which wraps the World with its Interaction Model.
 */
public class Controller {
    /**
     * Main world, where all simulated objects placed.
     */
    public World world;
    /**
     * Interaction Model, which governs how world is updated.
     */
    public InteractionModel interactionModel;

    public final int proportionCoef = 1;
    public final boolean doTurns = true;

    public final int mapSizeX = 45 * proportionCoef;
    public final int mapSizeY = 135 * proportionCoef;
    public final int amountOfPlates = 10 * proportionCoef;
    public final int temperatureBaseStart = -15;
    public final int temperatureBaseFinish = 29;
    /**
     * How many initial geology turns will pass in the beginning of the simulation.
     */
    public final int geologyTurns = 50;
    /**
     * How many turns will be spent before filling Resources in the World.
     */
    public final int initialTurns = 100;
    /**
     * How many turns will be spent after filling Resources in the World.
     */
    public final int stabilizationTurns = 100;
    public final int fillCycles = 3;
    /**
     * Below what level World will be covered under water.
     */
    public final int cultureTurns = 200;
    public final int defaultWaterLevel = 98;
    public final int startResourceAmountMin = 40 * proportionCoef * proportionCoef;
    public final int startResourceAmountMax = startResourceAmountMin + 30 * proportionCoef * proportionCoef;
    public final int startGroupAmount = 10;

    public final double tileScale = 10;

    public final double defaultGroupSpreadability = 1;
    public final int defaultGroupMaxPopulation = 1000;
    public final int defaultGroupMinPopulationPerTile = 1;
    public final int defaultGroupFertility = 10;
    public final double defaultGroupDiverge = (double) 1/100;
    public final double rAspectAcquisition = 0.01;
    public final double rAspectLending = 0.25;
    public final boolean groupDiverge = true;
    public final boolean subgroupMultiplication = false;

    public final double windPropagation = 0.025;
    public final double windFillIn = 0.1;
    public final double maximalWind = 10;

    public static Controller session;

    public String getVacantGroupName() {
        return "G" + world.groups.size();
    }

    /**
     * @param interactionModel  Interaction Model, which will govern how world will be updated.
     */
    public Controller(InteractionModel interactionModel) {
        session = this;
        world = new World();
        world.initializeZero();
        this.interactionModel = interactionModel;
    }

    public void initializeFirst() {
        for (int i = 0; i < geologyTurns; i++) {
            geologicTurn();
        }
        for (int i = 0; i < initialTurns; i++) {
            turn();
        }
        world.fillResources();
    }

    public void initializeSecond() {
        for (int j = 0; j < fillCycles && doTurns; j++) {
            if (j != 0) {
                world.fillResources();
            }
            for (int i = 0; i < stabilizationTurns; i++) {
                turn();
            }
        }
    }

    public void initializeThird() {
        world.initializeFirst();
        for (int i = 0; i < cultureTurns && doTurns
                ; i++) {
            turn();
        }
    }

    /**
     * Makes a turn in a simulation.
     */
    public void turn() {
        interactionModel.turn(world);
        world.incrementTurn();
    }

    public void geologicTurn() {
        interactionModel.geologicTurn(world);
        world.incrementTurnGeology();
    }
}
