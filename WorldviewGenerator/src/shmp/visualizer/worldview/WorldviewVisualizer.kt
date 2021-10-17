package shmp.visualizer.worldview

import shmp.generator.culture.worldview.Meme
import shmp.generator.culture.worldview.reasoning.ReasonField
import shmp.generator.culture.worldview.reasoning.convertion.baseConversions
import shmp.random.singleton.RandomSingleton
import kotlin.random.Random


class WorldviewVisualizer(private val reasonField: ReasonField) {
    fun run() {
        while (true) {
            for (i in 1..49)
                reasonField.update(listOf(Meme("We")))

            println(reasonField)

            readLine()
        }
    }
}


fun main() {
    if (RandomSingleton.safeRandom == null)
        RandomSingleton.safeRandom = Random(1)

    val reasonField = ReasonField(baseConversions())
    val visualizer = WorldviewVisualizer(reasonField)

    visualizer.run()
}
