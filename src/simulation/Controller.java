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

    public final double tileScale = 10;
    /**
     * How many initial geology turns will pass in the beginning of the simulation.
     */
    public final int geologyTurns = 50;
    /**
     * How many turns will be spent before filling Resources in the World.
     */
    public final int initialTurns = 100;
    public final int stabilizationTurns = 100;
    /**
     * Below what level World will be covered under water.
     */
    public final int defaultWaterLevel = 100;
    public final double defaultGroupSpreadability = 1;
    public final int defaultGroupMaxPopulation = 1000;
    public final int defaultGroupMinPopulationPerTile = 1;
    public final int defaultGroupFertility = 10;
    public final double defaultGroupDiverge = (double) 1/100;
    public final double rAspectAcquisition = 0.01;
    public final double rAspectLending = 0.25;
    public final boolean groupDiverge = true;

    public static Controller sessionController;

    public String getVacantGroupName() {
        return "G" + world.groups.size();
    }

    /**
     * Base constructor.
     *
     * @param numberOfGroups    how many groups will be spawned in the world.
     * @param mapSize           number from which map size will be computed. Guarantied that one of dimensions
     *                          will be equal to this number.
     * @param numberOrResources how many random resources will be created.
     * @param interactionModel  Interaction Model, which will govern how world will be updated.
     */
    public Controller(int numberOfGroups, int mapSize, int numberOrResources, InteractionModel interactionModel) {
        sessionController = this;
        world = new World(numberOfGroups, mapSize, numberOrResources);
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
        world.initializeFirst();
    }

    public void initializeSecond() {
        for (int i = 0; i < stabilizationTurns; i++) {
            turn();
        }
        world.initializeSecond();
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
