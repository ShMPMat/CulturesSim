package shmp.visualizer.text

import shmp.simulation.Controller
import shmp.simulation.CulturesController
import shmp.simulation.interactionmodel.CulturesMapModel
import shmp.simulation.interactionmodel.MapModel


fun main() {
    val textEcosystemVisualizer = TextEcosystemVisualizer(Controller(MapModel()))
    textEcosystemVisualizer.initialize()
    textEcosystemVisualizer.run()
}
