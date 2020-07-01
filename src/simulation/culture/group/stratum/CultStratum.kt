package simulation.culture.group.stratum

import simulation.culture.group.cultureaspect.SpecialPlace
import simulation.space.tile.Tile
import kotlin.math.max
import kotlin.math.min

class CultStratum(val cultName: String, tile: Tile) : NonAspectStratum(tile, "Stratum for $cultName") {
    override val freePopulation: Int
        get() = population

    override val places = listOf<SpecialPlace>()

    override fun useAmount(amount: Int, maxOverhead: Int): WorkerBunch {
        if (amount <= 0)
            return WorkerBunch(0, 0)
        val additional = max(0, min(amount - population, maxOverhead))
        population += additional
        val resultAmount = min(population, amount)
        return WorkerBunch(resultAmount, resultAmount)
    }

    override fun toString(): String {
        return "Stratum for $cultName, population - $population, importance - $importance" + super.toString()
    }
}
