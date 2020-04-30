package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack

interface Stratum {
    val population: Int
    val freePopulation: Int

    fun decreaseAmount(amount: Int)

    fun useCumulativeAmount(amount: Int): WorkerBunch

    fun useActualAmount(amount: Int): WorkerBunch

    fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group
    )

    fun finishUpdate(group: Group)

    fun die()
}

data class WorkerBunch(val cumulativeWorkers: Int, val actualWorkers: Int)