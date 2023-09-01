package io.tashtabash.sim.space.resource.container

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Taker
import kotlin.math.min


class ResourcePromise(val resource: Resource, amount: Int = resource.amount) {
    var amount = min(amount, resource.amount)
        private set

    fun extract(taker: Taker) = resource.getCleanPart(amount, taker)

    fun makeCopy() = resource.copy(amount)

    fun update() {
        amount = min(amount, resource.amount)
    }

    override fun toString() = "${resource.fullName} - $amount"
}
