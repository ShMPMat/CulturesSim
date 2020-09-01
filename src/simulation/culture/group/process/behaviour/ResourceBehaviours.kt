package simulation.culture.group.process.behaviour

import shmp.random.randomUnwrappedElementOrNull
import shmp.random.toSampleSpaceObject
import simulation.Controller
import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.emptyProcessResult
import simulation.culture.group.process.interaction.GiveGiftI


object GiveGiftB: AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val relatedGroups = group.relationCenter.relatedGroups
                .map { it.toSampleSpaceObject(group.relationCenter.getNormalizedRelation(group)) }
                .filter { it.probability > 0 }

        val receiver = randomUnwrappedElementOrNull(relatedGroups, Controller.session.random)
                ?: return emptyProcessResult

        return GiveGiftI(group, receiver).run()
    }

    override val internalToString = "Give a gift to a random Group"
}
