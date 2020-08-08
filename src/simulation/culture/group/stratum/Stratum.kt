package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.culture.group.place.StaticPlace
import simulation.space.Territory
import simulation.space.resource.container.MutableResourcePack
import kotlin.math.min


interface Stratum {
    val name: String

    val population: Int
    val freePopulation: Int
    val cumulativeWorkAblePopulation: Double

    var importance: Int

    val ego: Ego

    val places: List<StaticPlace>

    fun decreaseAmount(amount: Int)

    fun useAmount(amount: Int, maxOverhead: Int): StratumPeople

    fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group
    )

    fun finishUpdate(group: Group)

    fun die()
}


class StratumPeople(workers: Int, val stratum: Stratum, val effectiveness: Double = 1.0) {
    var workers = workers
        private set
    val cumulativeWorkers: Int
        get() = (workers * effectiveness).toInt()

    fun decreaseAmount(amount: Int) {
        val actualDecrease = min(amount, workers)

        workers -= actualDecrease
        stratum.decreaseAmount(actualDecrease)
    }

    override fun toString() = "$workers people, working as $cumulativeWorkers"
}
