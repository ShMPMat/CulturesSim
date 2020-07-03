package simulation.culture.group.stratum

import simulation.Controller
import simulation.SimulationException
import simulation.culture.aspect.getAspectImprovement
import simulation.culture.group.centers.Group
import simulation.culture.group.passingReward
import simulation.culture.group.request.AspectImprovementRequest
import simulation.culture.group.request.Request
import simulation.space.Territory
import simulation.space.resource.container.MutableResourcePack
import simulation.space.tile.Tile
import kotlin.math.max
import kotlin.math.min

class TraderStratum(tile: Tile) : NonAspectStratum(tile, "Stratum of traders") {
    private val tradeAspect = Controller.session.world.aspectPool.get("Trade")
            ?: throw SimulationException("No aspect Trade exists for the $name")

    private var _effectiveness = 1.0
        private set

    private var workedPopulation = 0
    override val freePopulation: Int
        get() = population - workedPopulation

    val effectiveness: Double
        get() {
            if (_effectiveness == -1.0)
                _effectiveness = 1.0 + places
                        .flatMap { it.owned.resources }
                        .map { it.getAspectImprovement(tradeAspect) }
                        .foldRight(0.0, Double::plus)
            if (_effectiveness > 1.0) {
                val k = 0
            }
            return _effectiveness
        }

    override fun useAmount(amount: Int, maxOverhead: Int): WorkerBunch {
        if (amount <= 0)
            return WorkerBunch(0, 0)

        usedThisTurn = true

        val additional = max(0, min(amount - population, maxOverhead))
        population += additional
        val resultAmount = min(population, amount)
        return WorkerBunch(resultAmount, resultAmount)
    }

    override fun update(accessibleResources: MutableResourcePack, accessibleTerritory: Territory, group: Group) {
        super.update(accessibleResources, accessibleTerritory, group)

        updatePlaces(group)
    }

    private fun updatePlaces(group: Group) {
        if (!Controller.session.isTime(Controller.session.stratumTurnsBeforeInstrumentRenewal))
            return

        if (!group.territoryCenter.settled)
            return

        val request: Request = AspectImprovementRequest(
                group,
                tradeAspect,
                0.5,
                0.5,
                passingReward,
                passingReward,
                30
        )
        val (pack, usedAspects) = group.populationCenter.executeRequest(request)
        if (pack.isNotEmpty) {
            val k = 0
        }

        usedAspects.forEach {
            it.gainUsefulness(Controller.session.stratumTurnsBeforeInstrumentRenewal * 2)
        }
        pack.resources.forEach { addEnhancement(it, group) }
    }

    override fun finishUpdate(group: Group) {
        _effectiveness = -1.0
        super.finishUpdate(group)
    }

    override fun toString() =
            "$name, population - $population, effectiveness - $effectiveness, importance - $importance" +
                    super.toString()
}
