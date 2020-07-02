package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.culture.group.place.StaticPlace
import simulation.space.Territory
import simulation.space.resource.container.MutableResourcePack

interface Stratum {
    val population: Int
    val freePopulation: Int
    var importance: Int
    val ego: Ego

    val places: List<StaticPlace>

    fun decreaseAmount(amount: Int)

    fun useAmount(amount: Int, maxOverhead: Int): WorkerBunch

    fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group
    )

    fun finishUpdate(group: Group)

    fun die()
}


data class WorkerBunch(val cumulativeWorkers: Int, val actualWorkers: Int)
