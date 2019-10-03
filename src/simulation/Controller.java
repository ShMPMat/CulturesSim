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
        world = new World(numberOfGroups, mapSize, numberOrResources);
        this.interactionModel = interactionModel;
    }

    /**
     * Makes a turn in a simulation.
     */
    public void turn() {
        interactionModel.turn(world);
        world.incrementTurn();
    }
}
