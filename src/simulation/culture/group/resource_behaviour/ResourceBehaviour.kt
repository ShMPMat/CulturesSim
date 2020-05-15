package simulation.culture.group.resource_behaviour

import shmp.random.randomElement
import simulation.culture.group.centers.Group
import simulation.space.resource.MutableResourcePack
import kotlin.random.Random

class ResourceBehaviour constructor(private val placementStrategy: PlacementStrategy) {
    fun proceedResources(resourcePack: MutableResourcePack) {
        placementStrategy.place(resourcePack)
    }

    override fun toString() = placementStrategy.toString()
}

fun getRandom(group: Group, random: Random): ResourceBehaviour = ResourceBehaviour(PlacementStrategy(
        group.territoryCenter.territory,
        randomElement(
                PlacementStrategy.Strategy.values().toList(),
                random
        )
))
