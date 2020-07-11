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

    override val internalToString = "Choose a random Neighbour and add it to the Conglomerate"
}


class RequestHelpB(val request: Request, val targetPack: MutableResourcePack) : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        if (request.ceiling <= 0)
            return emptyList()

        val events = mutableListOf<Event>()

        val amount = request.ceiling
        var amountLeft = amount
        for (relation in group.relationCenter.relations.sortedByDescending { it.positive }) {
            if (!testProbability(relation.normalized, Controller.session.random))
                continue

            val reducedRequest = request.reducedAmountCopy(amountLeft)
            val newEvents = RequestHelpInteraction(relation.owner, relation.other, reducedRequest).run()

            amountLeft = amount - newEvents
                    .mapNotNull { it.getAttribute("value")?.let { a -> a as Double } }
                    .foldRight(0.0, Double::plus)

            events.addAll(newEvents)
            if (amountLeft <= 0)
                break
        }

        return events
    }

    override val internalToString = "Ask help from all neighbours with $request if needed"
}

class TurnRequestsHelpB : AbstractGroupBehaviour() {
    override fun run(group: Group) = group.cultureCenter.requestCenter.turnRequests.requests
            .flatMap { (request, pack) ->
                RequestHelpB(request.reducedAmountCopy(request.amountLeft(pack)), pack).run(group)
            }

    override val internalToString = "Ask help from all neighbours with all base request if needed"
}
