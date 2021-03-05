package shmp.simulation.culture.group.stratum

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.tile.Tile
import kotlin.math.ceil
import kotlin.math.min

class WarriorStratum(tile: Tile) : NonAspectStratum(tile, "Stratum of warriors", "") {
    var _effectiveness = 1.0
        private set

    private var workedPopulation = 0
    override val freePopulation: Int
        get() = population - workedPopulation
    override val cumulativeWorkAblePopulation: Double
        get() = freePopulation * effectiveness

    private val effectiveness: Double
        get() {
            if (_effectiveness == -1.0) _effectiveness = 1.0
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