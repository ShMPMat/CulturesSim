package simulation.space.resource.container

import simulation.space.resource.Resource
import kotlin.math.min


class ResourcePromise(val resource: Resource, amount: Int = resource.amount) {
    var amount = min(amount, resource.amount)
        private set

    fun extract() = resource.getCleanPart(amount)

    fun makeCopy() = resource.copy(amount)

    fun update() {
        amount = min(amount, resource.amount)
    }

    override fun toString() = "${resource.fullName} - $amount"
}
