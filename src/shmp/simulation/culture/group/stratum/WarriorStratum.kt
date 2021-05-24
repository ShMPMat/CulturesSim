package shmp.simulation.culture.group.stratum

import shmp.random.singleton.chanceOfNot
import shmp.simulation.CulturesController.session
import shmp.simulation.SimulationError
import shmp.simulation.culture.aspect.getAspectImprovement
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.passingReward
import shmp.simulation.culture.group.request.AspectImprovementRequest
import shmp.simulation.culture.group.request.RequestCore
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.tile.Tile
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
