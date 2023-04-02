package io.tashtabash.simulation.culture.group.stratum

import io.tashtabash.random.singleton.chanceOfNot
import io.tashtabash.simulation.CulturesController.Companion.session
import io.tashtabash.simulation.SimulationError
import io.tashtabash.simulation.culture.aspect.getAspectImprovement
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.passingReward
import io.tashtabash.simulation.culture.group.request.AspectImprovementRequest
import io.tashtabash.simulation.culture.group.request.RequestCore
import io.tashtabash.simulation.culture.group.request.RequestType
import io.tashtabash.simulation.space.resource.container.MutableResourcePack
import io.tashtabash.simulation.space.territory.Territory
import io.tashtabash.simulation.space.tile.Tile
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


class WarriorStratum(tile: Tile) : NonAspectStratum(tile, "Stratum of warriors", "") {
    init {
        aspect = session.world.aspectPool.get("Killing")
                ?: throw SimulationError("No aspect Killing exists for the $name")
    }

    private var _effectiveness = 1.0

    private var workedPopulation = 0
    override val freePopulation: Int
        get() = population - workedPopulation
    override val cumulativeWorkAblePopulation: Double
        get() = freePopulation * effectiveness

    val effectiveness: Double
        get() {
            if (_effectiveness == -1.0) _effectiveness = 1.0 + places
                    .flatMap { it.owned.resources }
                    .map { it.getAspectImprovement(aspect!!) }
                    .foldRight(0.0, Double::plus)
            return _effectiveness
        }

    override fun useAmount(amount: Int, maxOverhead: Int): StratumPeople {
        if (amount <= 0)
            return StratumPeople(0, this)
        usedThisTurn = true
        var actualAmount = ceil(amount / effectiveness).toInt()
        if (actualAmount <= freePopulation)
            workedPopulation += actualAmount
        else {
            val additional = min(maxOverhead, actualAmount - freePopulation)
            actualAmount = additional + freePopulation
            population += additional
            workedPopulation = population
        }
        return StratumPeople(actualAmount, this, effectiveness)
    }

    override fun update(accessibleResources: MutableResourcePack, accessibleTerritory: Territory, group: Group) {
        super.update(accessibleResources, accessibleTerritory, group)
        session.stratumInstrumentRenewalProb.chanceOfNot {
            return
        }

        if (!group.territoryCenter.settled || population == 0)
            return

        if (30 + max(1, importance) > 100) {
            val k = 9
        }

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

    override fun decreaseAmount(amount: Int) {
        super.decreaseAmount(amount)
        if (workedPopulation > population)
            workedPopulation = population
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
