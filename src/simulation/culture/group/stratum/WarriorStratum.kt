package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.space.tile.Tile
import kotlin.math.ceil
import kotlin.math.min

class WarriorStratum(tile: Tile) : NonAspectStratum(tile, "Stratum of warriors") {
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

    override fun useAmount(amount: Int, maxOverhead: Int): WorkerBunch {
        if (amount <= 0)
            return WorkerBunch(0, 0)
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
        return WorkerBunch((actualAmount * effectiveness).toInt(), actualAmount)
    }

    override fun finishUpdate(group: Group) {
        _effectiveness = -1.0
        super.finishUpdate(group)
    }

    override fun toString() =
            "$name, population - $population, effectiveness - $effectiveness, importance - $importance" +
                    super.toString()
}
