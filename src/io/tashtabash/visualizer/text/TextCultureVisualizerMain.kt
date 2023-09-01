package io.tashtabash.visualizer.text

import io.tashtabash.sim.CulturesController
import io.tashtabash.sim.interactionmodel.CulturesMapModel


fun main() {
    val textCultureVisualizer = TextCultureVisualizer(CulturesController(CulturesMapModel()))
    textCultureVisualizer.initialize()
    textCultureVisualizer.run()
}
