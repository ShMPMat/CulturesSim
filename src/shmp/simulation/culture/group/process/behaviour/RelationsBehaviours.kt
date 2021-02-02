package shmp.simulation.culture.group.process.behaviour

import shmp.random.testProbability
import shmp.simulation.Controller
import shmp.simulation.culture.group.HelpEvent
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.emptyProcessResult
import shmp.simulation.culture.group.process.flatMapPR
import shmp.simulation.culture.group.process.interaction.RequestHelpI
import shmp.simulation.culture.group.request.Request
import kotlin.math.pow


class RequestHelpB(val request: Request) : AbstractGroupBehaviour() {
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
                RequestHelpB(request.reducedAmountCopy(request.amountLeft(pack))).run(group)
            }

    override val internalToString = "Ask help from all neighbours with all base request if needed"
}
