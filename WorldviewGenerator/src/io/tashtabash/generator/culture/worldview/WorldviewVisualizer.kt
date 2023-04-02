package io.tashtabash.generator.culture.worldview

import io.tashtabash.generator.culture.worldview.reasoning.ReasonField
import io.tashtabash.generator.culture.worldview.reasoning.concept.ideationalConcepts
import io.tashtabash.generator.culture.worldview.reasoning.convertion.baseConversions
import io.tashtabash.generator.culture.worldview.toMeme
import io.tashtabash.random.randomSublist
import io.tashtabash.random.singleton.RandomSingleton
import kotlin.random.Random


class WorldviewVisualizer(private val reasonField: ReasonField) {
    private val reasonerMemes = listOf("We".toMeme())

    fun run() {
        while (true) {
            for (i in 1..49)
                reasonField.update(reasonerMemes)

            println(reasonField)

            readLine()
        }
    }
}


fun main() {
    if (RandomSingleton.safeRandom == null)
        RandomSingleton.safeRandom = Random(1)

    val concepts = randomSublist(ideationalConcepts, RandomSingleton.random, 0, ideationalConcepts.size)
            .toSet()
    val reasonField = ReasonField(baseConversions(), specialConcepts = concepts)
    val visualizer = WorldviewVisualizer(reasonField)

    visualizer.run()
}
