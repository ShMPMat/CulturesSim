package io.tashtabash.sim.culture.group.request

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.space.resource.Resource

class SimpleResourceRequest(private val resource: Resource, core: RequestCore) : Request(core) {
    override fun reducedAmountCopy(amount: Double) =
            SimpleResourceRequest(resource, core.copy(floor = 0.0, ceiling = amount))

    override val evaluator = simpleResourceEvaluator(resource)

    override fun reassign(group: Group) = SimpleResourceRequest(resource, core.copy(group = group))

    override fun toString() = "Resource with simple name ${resource.simpleName}"
}

