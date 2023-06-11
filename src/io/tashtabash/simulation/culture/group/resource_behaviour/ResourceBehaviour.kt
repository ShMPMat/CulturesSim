package io.tashtabash.simulation.culture.group.resource_behaviour

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.simulation.space.resource.container.MutableResourcePack
import io.tashtabash.simulation.space.territory.Territory


class ResourceBehaviour(private val placementStrategy: PlacementStrategy) {
    fun proceedResources(resourcePack: MutableResourcePack, territory: Territory) {
        placementStrategy.place(resourcePack, territory)
    }

    override fun toString() = placementStrategy.toString()
}

fun getRandom() =
        ResourceBehaviour(PlacementStrategy(PlacementStrategy.Strategy.values().toList().randomElement()))
