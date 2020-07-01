package simulation.culture.group.stratum

import simulation.culture.group.cultureaspect.SpecialPlace
import simulation.space.tile.Tile
import kotlin.math.ceil
import kotlin.math.min

class WarriorStratum(tile: Tile) : NonAspectStratum(tile, "Stratum of warriors") {
    var _effectiveness = 1.0
        private set

    private var workedPopulation = 0
    override val freePopulation: Int
        get() = population - workedPopulation

    override val places = listOf<SpecialPlace>()

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
        return WorkerBunch(actualAmount, actualAmount)
    }


    override fun toString() =
            "Stratum of warriors, population - $population, effectiveness - $effectiveness, importance - $importance" +
                    super.toString()
}
