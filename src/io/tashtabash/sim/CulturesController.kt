package io.tashtabash.sim

import io.tashtabash.sim.culture.thinking.language.templates.TemplateBase
import io.tashtabash.sim.init.GroupTurnsStep
import io.tashtabash.sim.init.RegisterPersonStep
import io.tashtabash.sim.interactionmodel.InteractionModel


class CulturesController(
    interactionModel: InteractionModel<CulturesWorld>,
    culturesWorld: CulturesWorld
) : Controller<CulturesWorld>(interactionModel, culturesWorld) {
    var templateBase: TemplateBase

    val doPrint = false

    private var conglomerateCount = -1

    val cultureTurns = 0

    val startGroupAmount = (20 * proportionCoefficient).toInt()
    val defaultGroupSpreadability = 1.0
    val defaultGroupTraitSpread = 100.0
    val defaultGroupMaxPopulation = 100
    val defaultGroupTerritoryRadius = 6
    val defaultGroupReach = defaultGroupTerritoryRadius + 1
    val defaultGroupMinPopulationPerTile = 1
    val defaultGroupFertility = 10
    val defaultGroupExiting = 0.02
    val defaultGroupDiverge = 0.01
    val defaultTypeRenewal = 0.05
    val rAspectAcquisition = 1.0
    val groupCultureAspectCollapse = 0.05
    val groupCollapsedAspectUpdate = 0.01
    val groupAspectAdoptionProb = 0.07
    val groupDiverge = true
    val groupMultiplication = true
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

    val strayPlacesUpdate = 0.01

    val vacantGroupName: String
        get() {
            conglomerateCount++
            return "G$conglomerateCount"
        }

    init {
        session = this
        world.initializeMap(proportionCoefficient)
        templateBase = TemplateBase()

        initSteps += RegisterPersonStep()
        initSteps += GroupTurnsStep(cultureTurns, doPrint)
    }

    companion object {
        lateinit var session: CulturesController
    }
}


// Since the bad decision of using a singleton Controller has already been made,
// I decided to move the parameters here. Here they are at least available through
// the whole runtime. And since they aren't actually changed yet, I decided to go
// for an object for the time being
object CultureParameters {
    val defaultAspectUsefulness = 50
}
