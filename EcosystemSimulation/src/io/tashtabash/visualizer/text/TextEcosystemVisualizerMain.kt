package io.tashtabash.visualizer.text

import io.tashtabash.sim.Controller
import io.tashtabash.sim.World
import io.tashtabash.sim.interactionmodel.MapModel
import io.tashtabash.sim.space.resource.instantiation.tag.DefaultTagParser


fun main() {
    val world = World()
    val controller = Controller(MapModel(), world)
    val textEcosystemVisualizer = TextEcosystemVisualizer(controller)

    world.initializeMap(emptyMap(), DefaultTagParser(world.tags), listOf(), controller.proportionCoefficient)

    textEcosystemVisualizer.initialize()
    textEcosystemVisualizer.run()
}
