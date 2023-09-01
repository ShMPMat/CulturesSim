package io.tashtabash.sim.culture.group.resource_behaviour

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.territory.Territory


class ResourceBehaviour(private val placementStrategy: PlacementStrategy) {
    fun proceedResources(resourcePack: MutableResourcePack, territory: Territory) {
        placementStrategy.place(resourcePack, territory)
    }

    override fun toString() = placementStrategy.toString()
}

fun getRandom() =
        ResourceBehaviour(PlacementStrategy(PlacementStrategy.Strategy.values().toList().randomElement()))
