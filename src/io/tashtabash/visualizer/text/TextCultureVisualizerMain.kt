package io.tashtabash.visualizer.text

import io.tashtabash.simulation.CulturesController
import io.tashtabash.simulation.interactionmodel.CulturesMapModel


fun main() {
    val textCultureVisualizer = TextCultureVisualizer(CulturesController(CulturesMapModel()))
    textCultureVisualizer.initialize()
    textCultureVisualizer.run()
}
