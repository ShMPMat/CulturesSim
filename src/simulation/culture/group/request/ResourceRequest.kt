package simulation.culture.group.request

import simulation.culture.group.centers.Group
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.Resource

class ResourceRequest(private val resource: Resource, core: RequestCore) : Request(core) {
    override fun reducedAmountCopy(amount: Double) =
            ResourceRequest(resource, core.copy(floor = 0.0, ceiling = amount))

    override val evaluator = resourceEvaluator(resource)

    override fun reassign(group: Group) = ResourceRequest(resource, core.copy(group = group))

    override fun toString() = "Resource ${resource.baseName}"
}
