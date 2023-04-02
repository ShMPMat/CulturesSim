package io.tashtabash.simulation

import io.tashtabash.simulation.culture.thinking.language.templates.TemplateBase
import io.tashtabash.simulation.interactionmodel.InteractionModel


class CulturesController(
        interactionModel: InteractionModel<io.tashtabash.simulation.CulturesWorld>
) : Controller<io.tashtabash.simulation.CulturesWorld>(interactionModel, io.tashtabash.simulation.CulturesWorld()) {
    var templateBase: io.tashtabash.simulation.culture.thinking.language.templates.TemplateBase

    val doPrint = false

    private var conglomerateCount = -1

    val cultureTurns = 0

    val startGroupAmount = (20 * proportionCoefficient).toInt()
    val defaultGroupSpreadability = 1.0
    val defaultGroupTraitSpread = 100.0 //TODO back to 0?
    val defaultGroupMaxPopulation = 100
    val defaultGroupTerritoryRadius = 6
    val defaultGroupReach = defaultGroupTerritoryRadius + 1
    val defaultGroupMinPopulationPerTile = 1
    val defaultGroupFertility = 10
    val defaultGroupExiting = 0.02
    val defaultGroupDiverge = 0.01
    val defaultTypeRenewal = 0.05
    val rAspectAcquisition = 1.0
    val cultureAspectBaseProbability = 0.02
    val groupCultureAspectCollapse = 0.05
    val groupCollapsedAspectUpdate = 0.01
    val groupAspectAdoptionProb = 0.07
    val groupDiverge = true
    val groupMultiplication = true
    val independentCvSimpleAspectAdding = true
    val worshipPlaceProb = 0.1
    val placeSystemLimitsCheck = 0.05
    val egoRenewalProb = 0.08
    val reasoningUpdate = 0.1
    val behaviourUpdateProb = 0.1
    val memoryUpdateProb = 0.1
    val egoAcquisitionProb = 0.05
    val tradeStockUpdateProb = 0.1
    val resourceValueRefreshTime = 12.0
    val memoryStrengthCoefficient = 0.9
    val stratumInstrumentRenewalProb = 0.03

    val groupTurnsBetweenBorderCheck = 10
    val maxGroupDependencyDepth = 5
    val minimalStableFreePopulation = 10
    val aspectFalloff = -500
    val worshipFeatureFalloff = 100
    val defaultAspectUsefulness = 50

    val strayPlacesUpdate = 0.01

    val vacantGroupName: String
        get() {
            conglomerateCount++
            return "G$conglomerateCount"
        }

    init {
        io.tashtabash.simulation.CulturesController.Companion.session = this
        world.initializeMap(proportionCoefficient)
        templateBase = io.tashtabash.simulation.culture.thinking.language.templates.TemplateBase()

        initSteps += io.tashtabash.simulation.RegisterPersonStep()
        initSteps += io.tashtabash.simulation.GroupTurnsStep(cultureTurns, doPrint)
    }

    companion object {
        lateinit var session: io.tashtabash.simulation.CulturesController
    }
}
