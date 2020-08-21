package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller
import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.emptyProcessResult
import simulation.culture.group.process.interaction.GiveGiftI
import simulation.event.Event


object GiveGiftB: AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val relatedGroups = group.relationCenter.relatedGroups
                .map { it to group.relationCenter.getNormalizedRelation(group) }
                .filter { (_, n) -> n > 0 }


        if (relatedGroups.isEmpty())
            return emptyProcessResult

        val receiver = randomElement(relatedGroups, { (_, n) -> n}, Controller.session.random).first

        return GiveGiftI(group, receiver).run()
    }

    override val internalToString = "Give a gift to a random Group"
}
