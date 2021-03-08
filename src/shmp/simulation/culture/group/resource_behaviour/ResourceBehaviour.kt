package shmp.simulation.culture.group.resource_behaviour

import shmp.random.singleton.randomElement
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.territory.Territory


class ResourceBehaviour constructor(private val placementStrategy: PlacementStrategy) {
    fun proceedResources(resourcePack: MutableResourcePack, territory: Territory) {
        placementStrategy.place(resourcePack, territory)
    }

    override fun toString() = placementStrategy.toString()
}

fun getRandom(): ResourceBehaviour = ResourceBehaviour(PlacementStrategy(
        PlacementStrategy.Strategy.values().toList().randomElement()
))
