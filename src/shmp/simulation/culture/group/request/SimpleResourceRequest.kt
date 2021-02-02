package shmp.simulation.culture.group.request

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.resource.Resource

class SimpleResourceRequest(private val resource: Resource, core: RequestCore) : Request(core) {
    override fun reducedAmountCopy(amount: Double) =
            SimpleResourceRequest(resource, core.copy(floor = 0.0, ceiling = amount))

    override val evaluator = simpleResourceEvaluator(resource)

    override fun reassign(group: Group) = SimpleResourceRequest(resource, core.copy(group = group))

    override fun toString() = "Resource with simple name ${resource.simpleName}"
}

