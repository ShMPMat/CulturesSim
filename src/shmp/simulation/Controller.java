package shmp.simulation;

import kotlin.random.Random;
import kotlin.random.RandomKt;
import shmp.random.singleton.RandomSingleton;
import shmp.simulation.culture.thinking.language.templates.TemplateBase;
import shmp.simulation.interactionmodel.InteractionModel;
import shmp.simulation.space.LandscapeChangesKt;
import shmp.simulation.space.resource.Resource;
import shmp.simulation.space.tile.Tile;
import shmp.visualizer.Visualizer;


public class Controller {
    public static Controller session;
    public World world;
    public InteractionModel interactionModel;
    public TemplateBase templateBase;

    private int conglomerateCount = -1;

    public final int seed = (int) (Math.random() * 10000000);
    public final Random random = RandomKt.Random(8565728 + 4);

    public final int proportionCoefficient = 1;
    public final int resourceProportionCoefficient = 100;
    public final boolean doTurns = true;

    public final int geologyTurns = 50;
    public final int initialTurns = 100;
    public final int stabilizationTurns = 100;
    public final int fillCycles = 2;
    public final int cultureTurns = 0;

    public final int startResourceAmountMin = 40 * proportionCoefficient * proportionCoefficient;
    public final int startResourceAmountMax = startResourceAmountMin + 30 * proportionCoefficient * proportionCoefficient;

    public final int startGroupAmount = 10;
    public final double defaultGroupSpreadability = 1;
    public final int defaultGroupMaxPopulation = 100;
    public final int defaultGroupTerritoryRadius = 6;
    public final int defaultGroupReach = defaultGroupTerritoryRadius + 1;
    public final int defaultGroupMinPopulationPerTile = 1;
    public final int defaultGroupFertility = 10;
    public final double defaultGroupExiting = 0.02;
    public final double defaultGroupDiverge = 0.01;
    public final double defaultTypeRenewal = 0.05;
    public final double rAspectAcquisition = 1.0;
    public final double cultureAspectBaseProbability = 0.02;
    public final double groupCultureAspectCollapse = 0.05;
    public final double groupCollapsedAspectUpdate = 0.01;
    public final boolean groupDiverge = true;
    public final boolean groupMultiplication = true;
    public final boolean independentCvSimpleAspectAdding = true;
    public final double worshipPlaceProb = 0.1;
    public final double placeSystemLimitsCheck = 0.05;
    public final double egoRenewalProb = 0.08;
    public final double reasoningUpdate = 0.1;
    public final double behaviourUpdateProb = 0.1;
    public final double memoryUpdateProb = 0.1;
    public final double egoAcquisitionProb = 0.05;
    public final double tradeStockUpdateProb = 0.1;
    public final double resourceValueRefreshTime = 12;
    public final double memoryStrengthCoefficient = 0.9;

    public final int stratumTurnsBeforeInstrumentRenewal = 30;
    public final int groupTurnsBetweenBorderCheck = 10;
    public final int groupTurnsBetweenAdopts = 10;
    public final int maxGroupDependencyDepth = 5;
    public final int minimalStableFreePopulation = 10;
    public final int aspectFalloff = -500;
    public final int defaultAspectUsefulness = 50;

    public final double windFillIn = 0.1;

    public final double strayPlacesUpdate = 0.01;

    public static Visualizer visualizer;
    public static final boolean doPrint = false;

    public long overallTime = 0;
    public long groupTime = 0;
    public long othersTime = 0;
    public long groupMainTime = 0;
    public long groupOthersTime = 0;
    public long groupMigrationTime = 0;
    public long groupInnerOtherTime = 0;

    public String getVacantGroupName() {
        conglomerateCount++;
        return "G" + conglomerateCount;
    }

    public Controller(InteractionModel interactionModel) {
        session = this;
        RandomSingleton.INSTANCE.setSafeRandom(random);

        templateBase = new TemplateBase();
        world = new World(proportionCoefficient, random, "SupplementFiles");
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

    public void turn() {
        interactionModel.turn(world);
        world.incrementTurn();

        if (isTime(100)) {
            world.clearDeadConglomerates();
        }

        System.out.println(
                "Overall - " + overallTime + " Groups - " + groupTime + " Others - " + othersTime
              + " Groups to others - " + ((double) groupTime) / ((double) othersTime)
              + " main update to others - " + ((double) groupMainTime) / ((double) groupOthersTime)
              + " current test to others - " + ((double) groupMigrationTime) / ((double) groupInnerOtherTime)
        );
    }

    public void geologicTurn() {
        interactionModel.geologicTurn(world);
        world.incrementTurnGeology();
    }
}