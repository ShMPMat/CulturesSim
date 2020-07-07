package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller
import simulation.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.interaction.GroupTransferInteraction
import simulation.culture.group.process.interaction.RequestHelpInteraction
import simulation.culture.group.request.Request
import simulation.culture.group.request.RequestPool
import simulation.space.resource.container.MutableResourcePack


object RandomGroupAddB : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val options = group.territoryCenter.getAllNearGroups(group)
                .filter { it.parentGroup !== group.parentGroup }

        if (options.isNotEmpty()) {
            val target = randomElement(options, Controller.session.random)
            return GroupTransferInteraction(group, target).run()
        }
        return emptyList()
    }

    override fun toString() = "Choose a random Neighbour and add it to the Conglomerate"
}


class RequestHelpB(val request: Request, val targetPack: MutableResourcePack) : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        if (request.ceiling <= 0)
            return emptyList()

        val events = mutableListOf<Event>()

        val pack = MutableResourcePack()
        val amount = request.ceiling
        var amountLeft = amount
        for (relation in group.relationCenter.relations.sortedByDescending { it.positive }) {
            if (!testProbability(relation.normalized, Controller.session.random))
                continue

            val reducedRequest = request.reducedAmountCopy(amountLeft)
            events.addAll(RequestHelpInteraction(relation.owner, relation.other, reducedRequest, pack).run())

            amountLeft = amount - request.evaluator.evaluate(pack)
            if (amountLeft <= 0)
                break
        }

        targetPack.addAll(pack)

        return events
    }
}

class TurnRequestsHelpB : AbstractGroupBehaviour() {
    override fun run(group: Group) = group.cultureCenter.requestCenter.turnRequests.requests
            .flatMap { (request, pack) ->
                RequestHelpB(request.reducedAmountCopy(request.amountLeft(pack)), pack).run(group)
            }
}
