package io.tashtabash.simulation.culture.group.centers

import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.simulation.CulturesController.Companion.session
import io.tashtabash.simulation.culture.group.request.RequestPool
import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.container.MutableResourcePack
import io.tashtabash.simulation.space.resource.container.ResourcePack
import io.tashtabash.utils.MovingAverage


class MemoryCenter {
    var turnRequests = RequestPool(mapOf())
        internal set

    private val _resourceTraction = mutableMapOf<Resource, MovingAverage>()
    val resourceTraction: Map<Resource, MovingAverage>
        get() = _resourceTraction

    fun update(group: Group) {
        session.memoryUpdateProb.chanceOf {
            updateResourceTraction(group.territoryCenter.territory.allResourcesPack)
        }
    }

    fun updateResourceTraction(resourcePack: ResourcePack) {
        val newResources = MutableResourcePack()
        val oldResources = MutableResourcePack()

        for (resource in resourcePack.resources)
            if (_resourceTraction.containsKey(resource))
                oldResources.add(resource)
            else
                newResources.add(resource)

        for ((resource, average) in resourceTraction) {
            average.change(oldResources.getAmount(resource))
        }
        for (resource in newResources.resourcesIterator) {
            _resourceTraction[resource] = MovingAverage(resource.amount, session.memoryStrengthCoefficient)
        }
    }

    fun fullCopy() = MemoryCenter().apply {
        this.turnRequests = turnRequests
        this._resourceTraction.forEach { (r, ma) ->
            _resourceTraction[r] = MovingAverage(ma.value, ma.coefficient)
        }
    }

    override fun toString() = "$turnRequests\n" +
            "\n" +
            "Resources awareness\n" +
            resourceTraction.entries.sortedByDescending { it.value.value.actualValue }
                    .take(20)
                    .joinToString("\n") { (r, ma) ->
                        "${r.fullName} - ${ma.value.actualValue}"
                    }
}
