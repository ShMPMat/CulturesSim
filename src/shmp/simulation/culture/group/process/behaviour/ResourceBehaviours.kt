package shmp.simulation.culture.group.process.behaviour

import shmp.random.singleton.randomUnwrappedElementOrNull
import shmp.random.toSampleSpaceObject
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.emptyProcessResult
import shmp.simulation.culture.group.process.interaction.GiveGiftI


object GiveGiftB: AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val relatedGroups = group.relationCenter.relatedGroups
                .map { it.toSampleSpaceObject(group.relationCenter.getNormalizedRelation(group)) }
                .filter { it.probability > 0 }

        val receiver = relatedGroups.randomUnwrappedElementOrNull()
                ?: return emptyProcessResult

        return GiveGiftI(group, receiver).run()
    }

    override val internalToString = "Give a gift to a random Group"
}
