package io.tashtabash.visualizer.text

import io.tashtabash.sim.Controller
import io.tashtabash.sim.init.constructWorld
import io.tashtabash.sim.interactionmodel.MapModel
import io.tashtabash.sim.space.resource.instantiation.tag.DefaultTagParser


fun main() {
    val proportionCoefficient = 1.0

    val world = constructWorld(proportionCoefficient)
    val controller = Controller(MapModel(), world, proportionCoefficient)
    val textEcosystemVisualizer = TextEcosystemVisualizer(controller)

    world.initializeMap(emptyMap(), DefaultTagParser(world.tags), listOf(), proportionCoefficient)

    textEcosystemVisualizer.initialize()
    textEcosystemVisualizer.run()
}
