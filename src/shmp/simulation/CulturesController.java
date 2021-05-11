package shmp.simulation;

import shmp.simulation.culture.thinking.language.templates.TemplateBase;
import shmp.simulation.interactionmodel.InteractionModel;
import shmp.visualizer.Visualizer;


public class CulturesController extends Controller<CulturesWorld> {
    public static CulturesController session;
    public TemplateBase templateBase;

    private int conglomerateCount = -1;

    public final boolean doTurns = true;

    public final int cultureTurns = 0;

    public final int startGroupAmount = 10;
    public final double defaultGroupSpreadability = 1;
    public final double defaultGroupTraitSpread = 100;//TODO back to 0?
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
    public final double stratumInstrumentRenewalProb = 0.03;

    public final int groupTurnsBetweenBorderCheck = 10;
    public final int groupTurnsBetweenAdopts = 10;
    public final int maxGroupDependencyDepth = 5;
    public final int minimalStableFreePopulation = 10;
    public final int aspectFalloff = -500;
    public final int defaultAspectUsefulness = 50;

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

    public CulturesController(InteractionModel<CulturesWorld> interactionModel) {
        super(interactionModel);
        session = this;

        initializeWorld(new CulturesWorld());

        world.initializeMap(proportionCoefficient);

        templateBase = new TemplateBase();
    }


    public void initializeThird() {
        world.initializeGroups();
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
        super.turn();

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
