package shmp.visualizer.text

import shmp.simulation.CulturesController
import shmp.simulation.interactionmodel.CulturesMapModel


fun main() {
    val textEcosystemVisualizer = TextEcosystemVisualizer(CulturesController(CulturesMapModel()))
    textEcosystemVisualizer.initialize()
    textEcosystemVisualizer.run()
}
