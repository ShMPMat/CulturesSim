package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller
import simulation.culture.group.centers.Group
import simulation.culture.group.process.interaction.GiveGiftI
import simulation.event.Event


class GiveGiftB: AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val relatedGroups = group.relationCenter.relatedGroups
                .map { it to group.relationCenter.getNormalizedRelation(group) }
                .filter { (_, n) -> n > 0 }


        if (relatedGroups.isEmpty())
            return listOf()

        val receiver = randomElement(relatedGroups, { (_, n) -> n}, Controller.session.random).first

        return GiveGiftI(group, receiver).run()
    }

    override val internalToString = "Give a gift t oa random Group"
}
