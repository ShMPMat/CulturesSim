package shmp.visualizer.text

import shmp.simulation.CulturesController
import shmp.simulation.interactionmodel.CulturesMapModel


fun main() {
    val textCultureVisualizer = TextCultureVisualizer(CulturesController(CulturesMapModel()))
    textCultureVisualizer.initialize()
    textCultureVisualizer.run()
}
