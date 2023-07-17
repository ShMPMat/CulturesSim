package io.tashtabash.simulation.culture.group.process.behaviour

import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.simulation.CulturesController
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.cultureaspect.util.*
import io.tashtabash.simulation.culture.group.process.ProcessResult
import io.tashtabash.simulation.culture.group.process.emptyProcessResult
import io.tashtabash.simulation.culture.group.reason.constructBetterAspectUseReason
import io.tashtabash.simulation.event.Type
import io.tashtabash.simulation.event.of


object CreateCultureAspectsB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val cultureAspect = when (AspectRandom.values().randomElement()) {
            AspectRandom.AestheticallyPleasing -> createAestheticallyPleasingObject(
                    group.cultureCenter.aspectCenter.aspectPool.producedResources
                            .filter { it.genome.isDesirable }
                            .filter {
                                !group.cultureCenter.cultureAspectCenter.aestheticallyPleasingResources.contains(it)
                            }.maxByOrNull { it.genome.baseDesirability }
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


private enum class AspectRandom(override val probability: Double) : SampleSpaceObject {
    Tale(3.0),
    AestheticallyPleasing(1.0),
    Ritual(1.0),
    Concept(1.0)
}
