package shmp.simulation.culture.group.centers

import shmp.random.testProbability
import shmp.simulation.Controller.session
import shmp.simulation.culture.group.request.RequestPool
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.container.ResourcePack
import shmp.utils.MovingAverage


class MemoryCenter {
    var turnRequests = RequestPool(mapOf())
        internal set

    val _resourceTraction = mutableMapOf<Resource, MovingAverage>()
    val resourceTraction: Map<Resource, MovingAverage>
        get() = _resourceTraction

    fun update(group: Group) {
        if (testProbability(session.memoryUpdateProb, session.random))
            updateResourceTraction(group.territoryCenter.territory.allResourcesPack)
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
            _resourceTraction[resource.copy()] = MovingAverage(resource.amount, session.memoryStrengthCoefficient)
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
            resourceTraction.entries.sortedBy { it.value.value.actualValue }.joinToString("\n") { (r, ma) ->
                "${r.fullName} - ${ma.value.actualValue}"
            }
}
