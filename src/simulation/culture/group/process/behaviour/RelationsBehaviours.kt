package simulation.culture.group.process.behaviour

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.HelpEvent
import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.emptyProcessResult
import simulation.culture.group.process.flatMapPR
import simulation.culture.group.process.interaction.RequestHelpI
import simulation.culture.group.request.Request
import simulation.space.resource.container.MutableResourcePack
import kotlin.math.pow


class RequestHelpB(val request: Request, val targetPack: MutableResourcePack) : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        if (request.ceiling <= 0)
            return emptyProcessResult

        var processResult = emptyProcessResult

        val amount = request.ceiling
        var amountLeft = amount
        for (relation in group.relationCenter.relations.sortedByDescending { it.positive }) {
            if (!testProbability(relation.normalized.pow(2), Controller.session.random))
                break

            val reducedRequest = request.reducedAmountCopy(amountLeft)
            val newProcessResult = RequestHelpI(relation.owner, relation.other, reducedRequest).run()

            amountLeft = amount -
                    newProcessResult.events
                            .filterIsInstance<HelpEvent>()
                            .map { it.helpValue }
                            .foldRight(0.0, Double::plus)

            processResult += newProcessResult
            if (amountLeft <= 0)
                break
        }

        return processResult
    }

    override val internalToString = "Ask help from all neighbours with $request if needed"
}

object TurnRequestsHelpB : AbstractGroupBehaviour() {
    override fun run(group: Group) = group.cultureCenter.requestCenter.turnRequests.requests
            .flatMapPR { (request, pack) ->
                RequestHelpB(request.reducedAmountCopy(request.amountLeft(pack)), pack).run(group)
            }

    override val internalToString = "Ask help from all neighbours with all base request if needed"
}
