package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.space.Territory
import simulation.space.resource.container.MutableResourcePack
import simulation.space.tile.Tile
import kotlin.math.max
import kotlin.math.min

class CultStratum(val cultName: String, tile: Tile) : NonAspectStratum(tile, "Stratum for $cultName") {
    override val freePopulation: Int
        get() = population
    override val cumulativeWorkAblePopulation: Double
        get() = freePopulation.toDouble()

    override fun useAmount(amount: Int, maxOverhead: Int): StratumPeople {
        if (amount <= 0)
            return StratumPeople(0, this)

        val additional = max(0, min(amount - population, maxOverhead))
        population += additional
        val resultAmount = min(population, amount)
        return StratumPeople(resultAmount, this)
    }

    override fun update(accessibleResources: MutableResourcePack, accessibleTerritory: Territory, group: Group) {
        usedThisTurn = true
        super.update(accessibleResources, accessibleTerritory, group)
    }

    override fun toString() = "$name, population - $population, importance - $importance" + super.toString()
}
