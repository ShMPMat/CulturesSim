package simulation.culture.group.resource_behaviour

import shmp.random.randomElement
import simulation.culture.group.Group
import simulation.space.resource.ResourcePack
import kotlin.random.Random

class ResourceBehaviour(private val placementStrategy: PlacementStrategy) {
    fun proceedResources(resourcePack: ResourcePack) {
        placementStrategy.place(resourcePack)
    }

    override fun toString() = placementStrategy.toString()
}

fun getRandom(group: Group, random: Random): ResourceBehaviour = ResourceBehaviour(PlacementStrategy(
        group.overallTerritory,
        randomElement(
                PlacementStrategy.Strategy.values().toList(),
                random
        )
))
