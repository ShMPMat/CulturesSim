package shmp.simulation.culture.group.cultureaspect.worship

import shmp.simulation.culture.aspect.hasMeaning
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.resourceToRequest
import shmp.simulation.space.resource.Resource


class Fetish(val resource: Resource) : WorshipFeature {
    override var isFunctioning = false
        private set

    override fun use(group: Group, parent: Worship) {
        val neededAmount = group.populationCenter.population
        var diff = neededAmount - group.resourceCenter.getResource(resource).amount

        if (diff > 0) {
            isFunctioning = false
            val request = resourceToRequest(resource, group, diff, diff, setOf(RequestType.Spiritual))
            val result = group.populationCenter.executeRequest(request)

            val gotten = result.pack
            diff -= gotten.amount

            if (diff > 0)
                group.resourceCenter.addNeeded(request.evaluator.labeler, diff * 5)

            group.resourceCenter.addAll(gotten)
        } else isFunctioning = true
    }

    override fun adopt(group: Group) = Fetish(resource)

    override fun die(group: Group, parent: Worship) = Unit

    override fun swapWorship(worshipObject: WorshipObject) = Fetish(resource)

    override fun toString() = "Fetish of ${resource.fullName}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Fetish) return false

        if (resource != other.resource) return false

        return true
    }

    override fun hashCode(): Int {
        return resource.hashCode()
    }
}

