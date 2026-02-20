package io.tashtabash.sim.culture.group.centers

import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.culture.group.request.RequestPool
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.resource.container.ResourcePack
import io.tashtabash.utils.MovingAverage


class MemoryCenter(
    private val _resourceTraction: MutableMap<Resource, MovingAverage> = mutableMapOf(),
    turnRequests: RequestPool = RequestPool(mapOf())
) {
    var turnRequests = turnRequests
        internal set

    val resourceTraction: Map<Resource, MovingAverage>
        get() = _resourceTraction

    fun updateResourceTraction(resourcePack: ResourcePack) {
        val newResources = MutableResourcePack()
        val oldResources = MutableResourcePack()

        for (resource in resourcePack.resources)
            if (_resourceTraction.containsKey(resource))
                oldResources.add(resource)
            else
                newResources.add(resource)

        for ((resource, average) in resourceTraction)
            average.change(oldResources.getAmount(resource))
        for (resource in newResources.resourcesIterator)
            _resourceTraction[resource] = MovingAverage(resource.amount, session.memoryStrengthCoefficient)
    }

    fun fullCopy() = MemoryCenter(resourceTraction.toMutableMap(), turnRequests)

    override fun toString() = "Resources awareness\n" +
            resourceTraction.entries.sortedByDescending { it.value.value.actualValue }
                .take(20)
                .joinToString("\n") { (r, ma) ->
                    "${r.fullName} - ${ma.value.actualValue}"
                }
}
