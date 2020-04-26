package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack

class CultStratum(val cultName: String) : Stratum {
    override var population: Int = 1
        private set

    override val freePopulation: Int
        get() = population

    override fun decreaseAmount(amount: Int) {
        population -= amount
    }

    override fun useCumulativeAmount(amount: Int): WorkerBunch {
        if (amount <= 0)
            return WorkerBunch(0, 0)
        if (population < amount)
            population = amount
        return WorkerBunch(amount, amount)
    }

    override fun useActualAmount(amount: Int) = useCumulativeAmount(amount)

    override fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group
    ) {

    }

    override fun finishUpdate(group: Group) {}

    override fun toString(): String {
        return "Stratum for $cultName, population - $population"
    }
}
