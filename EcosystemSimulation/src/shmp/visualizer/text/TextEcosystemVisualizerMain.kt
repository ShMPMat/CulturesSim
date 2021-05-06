package shmp.visualizer.text

import shmp.simulation.Controller
import shmp.simulation.World
import shmp.simulation.interactionmodel.MapModel
import shmp.simulation.space.resource.instantiation.DefaultTagParser


fun main() {
    val controller = Controller(MapModel())
    val world = World("SupplementFiles")
    val textEcosystemVisualizer = TextEcosystemVisualizer(controller)

    controller.initializeWorld(world)
    world.initializeMap(emptyList(), DefaultTagParser(world.tags), controller.proportionCoefficient)

    textEcosystemVisualizer.initialize()
    textEcosystemVisualizer.run()
}
