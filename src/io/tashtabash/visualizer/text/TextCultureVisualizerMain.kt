package io.tashtabash.visualizer.text

import io.tashtabash.sim.CulturesController
import io.tashtabash.sim.CulturesWorld
import io.tashtabash.sim.EcosystemWorld
import io.tashtabash.sim.interactionmodel.CulturesMapModel


fun main() {
    val world = CulturesWorld(EcosystemWorld())
    val controller = CulturesController(CulturesMapModel(), world)
    val textCultureVisualizer = TextCultureVisualizer(controller)
    textCultureVisualizer.initialize()
    textCultureVisualizer.run()
}
