package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack
import kotlin.math.ceil
import kotlin.math.min

class WarriorStratum : Stratum {
    var unusedTurns = 0
        private set
    private var usedThisTurn = false
    var _effectiveness = 1.0
        private set

    private fun getEffectiveness(): Double {
        if (_effectiveness == -1.0) _effectiveness = 1.0
        return _effectiveness
    }

    private var workedPopulation = 0

    override var population: Int = 0
        private set

    override val freePopulation: Int
        get() = population - workedPopulation

    override fun decreaseAmount(amount: Int) {
        population -= amount
    }

    override fun useAmount(amount: Int, maxOverhead: Int): WorkerBunch {
        if (amount <= 0) {
            return WorkerBunch(0, 0)
        }
        usedThisTurn = true
        var actualAmount = ceil(amount / getEffectiveness()).toInt()
        if (actualAmount <= freePopulation) {
            workedPopulation += actualAmount
        } else {
            val additional = min(maxOverhead, actualAmount - freePopulation)
            actualAmount = additional + freePopulation
            population += additional
            workedPopulation = population
//            if (additional > 0) {
//                isRaisedAmount = true
//            }
        }
        return WorkerBunch(actualAmount, actualAmount)
    }

    override fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group
    ) {
    }

    override fun finishUpdate(group: Group) {
        if (usedThisTurn) unusedTurns = 0
        else unusedTurns++
        usedThisTurn = false
    }

    override fun die() {
        population = 0
    }

    override fun toString(): String {
        return "Stratum of warriors, population - $population"
    }
}
