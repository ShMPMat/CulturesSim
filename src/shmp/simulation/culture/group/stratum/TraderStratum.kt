package shmp.simulation.culture.group.stratum

import shmp.random.testProbability
import shmp.simulation.Controller.*
import shmp.simulation.SimulationError
import shmp.simulation.culture.aspect.getAspectImprovement
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.passingReward
import shmp.simulation.culture.group.request.AspectImprovementRequest
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.group.request.RequestCore
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.container.ResourcePromise
import shmp.simulation.space.resource.container.ResourcePromisePack
import shmp.simulation.space.tile.Tile
import kotlin.math.max
import kotlin.math.min

class TraderStratum(tile: Tile) : NonAspectStratum(tile, "Stratum of traders") {
    private val tradeAspect = session.world.aspectPool.get("Trade")
            ?: throw SimulationError("No aspect Trade exists for the $name")

    private var _effectiveness = 1.0
        private set

    private var workedPopulation = 0
    override val freePopulation: Int
        get() = population - workedPopulation
    override val cumulativeWorkAblePopulation: Double
        get() = freePopulation * effectiveness

    var stock = ResourcePromisePack()
        private set

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

    override fun useAmount(amount: Int, maxOverhead: Int): StratumPeople {
        if (amount <= 0)
            return StratumPeople(0, this)

        usedThisTurn = true

        val additional = max(0, min(amount - population, maxOverhead))
        population += additional
        val resultAmount = min(population, amount)
        return StratumPeople(resultAmount, this, effectiveness)
    }

    override fun decreaseAmount(amount: Int) {
        super.decreaseAmount(amount)
        if (workedPopulation > population)
            workedPopulation = population
    }

    override fun update(accessibleResources: MutableResourcePack, accessibleTerritory: Territory, group: Group) {
        super.update(accessibleResources, accessibleTerritory, group)

        if (testProbability(session.tradeStockUpdateProb, session.random))
            tradeStockUpdate(group)

        updatePlaces(group)
    }

    fun tradeStockUpdate(group: Group) {
        val valuableResources = group.populationCenter.turnResources.resources
                .map { it to group.cultureCenter.evaluateResource(it) }
                .filter { (r, n) -> r.genome.isMovable && n >= 10 }
                .map { (r, _) -> ResourcePromise(r) }
        stock = ResourcePromisePack(valuableResources)
    }


    private fun updatePlaces(group: Group) {
        if (!session.isTime(session.stratumTurnsBeforeInstrumentRenewal))
            return

        if (!group.territoryCenter.settled)
            return

        val request: Request = AspectImprovementRequest(
                tradeAspect,
                RequestCore(
                        group,
                        0.5,
                        0.5,
                        passingReward,
                        passingReward,
                        30
                )
        )
        val (pack, usedAspects) = group.populationCenter.executeRequest(request)
        if (pack.isNotEmpty) {
            val k = 0
        }

        usedAspects.forEach {
            it.gainUsefulness(session.stratumTurnsBeforeInstrumentRenewal * 2)
        }
        pack.resources.forEach { addEnhancement(it, group) }
    }

    override fun finishUpdate(group: Group) {
        _effectiveness = -1.0
        workedPopulation = 0
        super.finishUpdate(group)
    }

    override fun toString() =
            "$name, population - $population, effectiveness - $effectiveness, importance - $importance" +
                    super.toString()
}
