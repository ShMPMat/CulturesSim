package io.tashtabash.sim.culture.group.stratum

import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.chanceOfNot
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.SimulationError
import io.tashtabash.sim.culture.aspect.getAspectImprovement
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.passingReward
import io.tashtabash.sim.culture.group.request.AspectImprovementRequest
import io.tashtabash.sim.culture.group.request.RequestCore
import io.tashtabash.sim.culture.group.request.RequestType
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.resource.container.ResourcePromise
import io.tashtabash.sim.space.resource.container.ResourcePromisePack
import io.tashtabash.sim.space.territory.Territory
import io.tashtabash.sim.space.tile.Tile
import kotlin.math.max
import kotlin.math.min


class TraderStratum(tile: Tile) : NonAspectStratum(tile, "Stratum of traders", "") {
    init {
        aspect = session.world.aspectPool.get("Trade")
                ?: throw SimulationError("No aspect Trade exists for the $name")
    }

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
                        .map { it.getAspectImprovement(aspect!!) }
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

        session.tradeStockUpdateProb.chanceOf {
            tradeStockUpdate(group)
        }

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
        session.stratumInstrumentRenewalProb.chanceOfNot {
            return
        }

        if (!group.territoryCenter.settled || population == 0)
            return

        val request = AspectImprovementRequest(
                aspect!!,
                RequestCore(
                        group,
                        0.5,
                        0.5 * max(1, importance),
                        passingReward,
                        passingReward,
                        30 + max(1, importance),
                        setOf(RequestType.Improvement)
                )
        )
        val (pack, usedAspects) = group.populationCenter.executeRequest(request)

        usedAspects.forEach {
            it.gainUsefulness((2 / session.stratumInstrumentRenewalProb).toInt())
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
