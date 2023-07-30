package io.tashtabash.simulation.culture.group.process.behaviour

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.process.ProcessResult
import io.tashtabash.simulation.event.Event
import io.tashtabash.simulation.event.Type
import io.tashtabash.simulation.space.resource.Taker
import io.tashtabash.simulation.space.resource.tag.ResourceTag
import io.tashtabash.simulation.space.resource.tag.labeler.BaseNameLabeler
import io.tashtabash.simulation.space.resource.tag.labeler.TagLabeler


object DefenceFromNatureB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val defenceEvents = mutableListOf<Event>()

        group.populationCenter.actualPopulation.takers
                .filter { it.first is Taker.ResourceTaker && it.second > 0 }
                .forEach { (taker, decrease) ->
                    val hostileResource = (taker as Taker.ResourceTaker).resource

                    hostileResource.genome.parts.firstOrNull()?.let { part ->
                        group.resourceCenter.addNeeded(BaseNameLabeler(part.baseName), decrease * 100)
                    }
                    group.resourceCenter.addNeeded(TagLabeler(ResourceTag("weapon")), decrease * 100)
                    group.resourceCenter.addNeeded(TagLabeler(ResourceTag("defence")), decrease * 100)

                    defenceEvents += Event(
                            Type.Conflict,
                            "${group.name} increased defence against ${taker.resource.baseName}"
                    )
                }

        return ProcessResult(events = defenceEvents)
    }

    override val internalToString = "Monitor surrounding dangers and confront them"
}
