package io.tashtabash.visualizer.text

import io.tashtabash.sim.CulturesController
import io.tashtabash.sim.CulturesWorld
import io.tashtabash.sim.culture.aspect.AspectInstantiation
import io.tashtabash.sim.culture.aspect.MutableAspectPool
import io.tashtabash.sim.init.constructWorld
import io.tashtabash.sim.interactionmodel.CulturesMapModel
import io.tashtabash.sim.space.resource.action.ActionTag
import io.tashtabash.sim.space.resource.tag.ResourceTag


fun main() {
    val proportionCoefficient = 1.0

    val ecosystemWorld = constructWorld(proportionCoefficient)
    val aspectPool = loadAspects(ecosystemWorld.tags, ecosystemWorld.actionTags)
    val world = CulturesWorld(ecosystemWorld, aspectPool)
    val controller = CulturesController(CulturesMapModel(), world, proportionCoefficient)
    val textCultureVisualizer = TextCultureVisualizer(controller)
    textCultureVisualizer.initialize()

    textCultureVisualizer.run()
}


fun loadAspects(tags: Set<ResourceTag>, actionTags: List<ActionTag>): MutableAspectPool {
    val classLoader = Thread.currentThread().contextClassLoader
    val aspectUrls = classLoader.getResources("Aspects")

    return AspectInstantiation(tags, actionTags).createPool(aspectUrls)
}
