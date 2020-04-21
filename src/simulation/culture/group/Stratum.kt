package simulation.culture.group

import simulation.culture.group.centers.Group
import simulation.culture.group.centers.PopulationCenter
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack

interface Stratum {
    val population: Int

    fun decreaseAmount(delta: Int)

    fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            populationCenter: PopulationCenter
    )

    fun finishUpdate(group: Group)
}