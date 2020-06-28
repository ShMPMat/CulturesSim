package simulation.space.resource.container

import simulation.space.resource.Resource
import kotlin.math.min

class ResourcePromise(val resource: Resource, amount: Int) {
    val amount = min(amount, resource.amount)

    fun extract() = resource.getCleanPart(amount)

    fun makeCopy() = resource.copy(amount)
}
