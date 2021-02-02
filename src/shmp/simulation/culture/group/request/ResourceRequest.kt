package shmp.simulation.culture.group.request

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.resource.Resource

class ResourceRequest(private val resource: Resource, core: RequestCore) : Request(core) {
    override fun reducedAmountCopy(amount: Double) =
            ResourceRequest(resource, core.copy(floor = 0.0, ceiling = amount))

    override val evaluator = resourceEvaluator(resource)

    override fun reassign(group: Group) = ResourceRequest(resource, core.copy(group = group))

    override fun toString() = "Resource ${resource.baseName}"
}
