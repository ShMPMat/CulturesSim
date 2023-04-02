package io.tashtabash.visualizer.text

import io.tashtabash.simulation.Controller
import io.tashtabash.simulation.World
import io.tashtabash.simulation.interactionmodel.MapModel
import io.tashtabash.simulation.space.resource.instantiation.tag.DefaultTagParser


fun main() {
    val world = World()
    val controller = Controller(MapModel(), world)
    val textEcosystemVisualizer = TextEcosystemVisualizer(controller)

    world.initializeMap(emptyMap(), DefaultTagParser(world.tags), listOf(), controller.proportionCoefficient)

    textEcosystemVisualizer.initialize()
    textEcosystemVisualizer.run()
}
