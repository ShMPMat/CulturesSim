package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.generator.culture.worldview.toMeme
import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.sim.CulturesController
import io.tashtabash.sim.culture.desirableTag
import io.tashtabash.sim.culture.group.GROUP_TAG_TYPE
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.cultureaspect.util.*
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.culture.group.reason.constructBetterAspectUseReason
import io.tashtabash.sim.event.Type
import io.tashtabash.sim.event.of


object CreateCultureAspectsB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val cultureAspect = when (AspectRandom.values().randomElement()) {
            AspectRandom.AestheticallyPleasing -> createAestheticallyPleasingObject(
                    group.cultureCenter.aspectCenter.aspectPool.producedResources
                            .filter { it.genome.getTagLevel(desirableTag) > 0 }
                            .filter {
                                !group.cultureCenter.cultureAspectCenter.aestheticallyPleasingResources.contains(it)
                            }
                            .filter { it.genome.baseDesirability > 0 }
                            .randomElementOrNull { it.genome.baseDesirability.toDouble() }
            )
            AspectRandom.Ritual -> createRitual(//TODO recursively go in dependencies;
                    constructBetterAspectUseReason(
                            group.cultureCenter.aspectCenter.aspectPool.converseWrappers.sortedBy { it.name },
                            group.cultureCenter.cultureAspectCenter.reasonsWithSystems,
                            CulturesController.session.random
                    ),
                    group,
                    CulturesController.session.random
            )
            AspectRandom.Tale -> createTale(
                    group,
                    CulturesController.session.templateBase
            )
            AspectRandom.Concept -> createSimpleConcept(
                    group,
                    CulturesController.session.random
            )
        }

        return if (group.cultureCenter.cultureAspectCenter.addCultureAspect(cultureAspect))
            ProcessResult(Type.AspectGaining of "${group.name} gained a random culture aspect $cultureAspect")
        else emptyProcessResult
    }

    override val internalToString = "Create culture aspects"
}

object PerceiveSurroundingTerritoryB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val memes = group.territoryCenter
            .accessibleTerritory
            .tiles
            .flatMap { it.tagPool.all }
            .filter { it.type != GROUP_TAG_TYPE }
            .map { it.name.toMeme() }

        return ProcessResult(memes)
    }

    override val internalToString = "Perceive surrounding territory"
}


private enum class AspectRandom(override val probability: Double) : SampleSpaceObject {
    Tale(3.0),
    AestheticallyPleasing(1.0),
    Ritual(1.0),
    Concept(1.0)
}
