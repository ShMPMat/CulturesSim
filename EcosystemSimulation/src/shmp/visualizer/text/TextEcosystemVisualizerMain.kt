package shmp.visualizer.text

import shmp.simulation.Controller
import shmp.simulation.World
import shmp.simulation.interactionmodel.MapModel
import shmp.simulation.space.resource.instantiation.tag.DefaultTagParser


fun main() {
    val world = World()
    val controller = Controller(MapModel(), world)
    val textEcosystemVisualizer = TextEcosystemVisualizer(controller)

    world.initializeMap(emptyMap(), DefaultTagParser(world.tags), listOf(), controller.proportionCoefficient)

    textEcosystemVisualizer.initialize()
    textEcosystemVisualizer.run()
}
