package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.culture.group.cultureaspect.Worship
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack
import kotlin.math.max
import kotlin.math.min

class CultStratum(val cultName: String) : Stratum {
    override var population: Int = 0
        private set
    override val freePopulation: Int
        get() = population
    override val importance: Int
        get() = population

    override fun decreaseAmount(amount: Int) {
        population -= amount
    }

    override fun useAmount(amount: Int, maxOverhead: Int): WorkerBunch {
        if (amount <= 0)
            return WorkerBunch(0, 0)
        val additional = max(0, min(amount - population, maxOverhead))
        population += additional
        val resultAmount = min(population, amount)
        return WorkerBunch(resultAmount, resultAmount)
    }

    override fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group
    ) {
    }

    override fun finishUpdate(group: Group) {}

    override fun die() {
        population = 0
    }

    override fun toString(): String {
        return "Stratum for $cultName, population - $population, importance - $importance"
    }
}
