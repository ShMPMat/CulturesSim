package shmp.visualizer.text

import shmp.simulation.Controller
import shmp.simulation.interactionmodel.MapModel


fun main() {
    val textCultureVisualizer = TextCultureVisualizer(Controller(MapModel()))
    textCultureVisualizer.initialize()
    textCultureVisualizer.run()
}
