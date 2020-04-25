package simulation.culture.group

import simulation.culture.group.centers.Group
import simulation.culture.group.centers.PopulationCenter
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack

interface Stratum {
    val population: Int

    fun decreaseAmount(amount: Int)

    fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group
    )

    fun finishUpdate(group: Group)
}

data class WorkerBunch(val cumulativeWorkers: Int, val actualWorkers: Int)