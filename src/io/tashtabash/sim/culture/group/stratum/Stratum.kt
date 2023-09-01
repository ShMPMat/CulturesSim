package io.tashtabash.sim.culture.group.stratum

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.place.StaticPlace
import io.tashtabash.sim.space.territory.Territory
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import kotlin.math.min


interface Stratum {
    val baseName: String
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
