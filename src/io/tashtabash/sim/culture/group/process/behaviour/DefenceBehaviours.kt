package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.sim.culture.defenceTag
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.culture.weaponTag
import io.tashtabash.sim.event.Event
import io.tashtabash.sim.event.Type
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.sim.space.resource.tag.labeler.BaseNameLabeler
import io.tashtabash.sim.space.resource.tag.labeler.TagLabeler


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
                group.resourceCenter.addNeeded(TagLabeler(weaponTag), decrease * 100)
                group.resourceCenter.addNeeded(TagLabeler(defenceTag), decrease * 100)

                defenceEvents += Event(
                    Type.Conflict,
                    "${group.name} increased defence against ${taker.resource.baseName}"
                )
            }

        return ProcessResult(events = defenceEvents)
    }

    override val internalToString = "Monitor surrounding dangers and confront them"
}


object ManageDefenceB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val additionalDanger = group.resourceCenter.pack.getTagPresence(weaponTag) / 1000
        val additionalResistance = group.resourceCenter.pack.getTagPresence(defenceTag) / 1000

        group.populationCenter.actualPopulation.genome.behaviour.apply {
            danger = 0.05 + additionalDanger
            resistance = 0.1 + additionalResistance
        }

        return emptyProcessResult
    }

    override val internalToString = "Monitor surrounding dangers and confront them"
}
