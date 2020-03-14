package simulation;

import kotlin.random.Random;
import kotlin.random.RandomKt;
import simulation.interactionmodel.InteractionModel;
import simulation.culture.thinking.language.templates.TemplateBase;
import visualizer.Visualizer;

/**
 * Class which wraps the World with its Interaction Model.
 */
public class Controller {
    public static Controller session;
    /**
     * Main world, where all simulated objects placed.
     */
    public World world;
    public InteractionModel interactionModel;
    public TemplateBase templateBase;

    public final int seed = (int) (Math.random() * 10000000);
    public final Random random = RandomKt.Random(8565728 + 1);

    public final int proportionCoefficient = 1;
    public final boolean doTurns = true;

    public final int mapSizeX = 45 * proportionCoefficient;
    public final int mapSizeY = 135 * proportionCoefficient;
    public final int platesAmount = 10 * proportionCoefficient;
    public final int temperatureBaseStart = -15;
    public final int temperatureBaseFinish = 29;
    public final int geologyTurns = 50;
    public final int initialTurns = 100;
    public final int stabilizationTurns = 100;
    public final int fillCycles = 2;
    public final int cultureTurns = 300;
    public final int defaultWaterLevel = 98;
    public final int startResourceAmountMin = 40 * proportionCoefficient * proportionCoefficient;
    public final int startResourceAmountMax = startResourceAmountMin + 30 * proportionCoefficient * proportionCoefficient;
    public final int startGroupAmount = 10;

    public final double  defaultGroupSpreadability        = 1;
    public final int     defaultGroupMaxPopulation        = 1000;
    public final int     defaultGroupMinPopulationPerTile = 1;
    public final int     defaultGroupFertility            = 10;
    public final double  defaultGroupExiting              = 0.0005;
    public final double  defaultGroupDiverge              = 0.002;
    public final double  rAspectAcquisition               = 0.1;
    public final double  cultureAspectBaseProbability     = 0.02;
    public final double  groupCultureAspectCollapse       = 0.01;
    public final double  groupCollapsedAspectUpdate       = 0.01;
    public final boolean groupDiverge                     = true;
    public final boolean groupMultiplication              = true;
    public final boolean independentCvSimpleAspectAdding  = true;

    public final int stratumTurnsBeforeInstrumentRenewal = 30;
    public final int groupTurnsBetweenBorderCheck = 10;
    public final int groupTurnsBetweenAdopts = 10;

    public final double windFillIn = 0.1;

    public static Visualizer visualizer;
    public static final boolean doPrint = false;

    public String getVacantGroupName() {
        return "G" + world.groups.size();
    }

    /**
     * @param interactionModel  Interaction Model, which will govern how world will be updated.
     */
    public Controller(InteractionModel interactionModel) {
        session = this;
        templateBase = new TemplateBase();
        world = new World();
        this.interactionModel = interactionModel;
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
        }
    }

    public void initializeThird() {
        world.initializeFirst();
        for (int i = 0; i < cultureTurns && doTurns; i++) {
            turn();
            if (doPrint) {
                visualizer.print();
            }
        }
    }

    public boolean isTime(int denominator) {
        return world.getLesserTurnNumber() % denominator == 0;
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
