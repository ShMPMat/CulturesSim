package shmp.simulation.culture.group.stratum

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.tile.Tile
import kotlin.math.max
import kotlin.math.min

class CultStratum(val cultName: String, tile: Tile) : NonAspectStratum(tile, "Stratum for cult ", cultName) {
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
