package shmp.simulation.culture.group.cultureaspect.worship

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.ResourcePack


class Fetish(resource: Resource) : WorshipResourceFeature(resource) {
    override fun doUse(group: Group, parent: Worship) = true

    override fun getNeededAmount(group: Group, parent: Worship) = group.populationCenter.population -
            group.resourceCenter.getResource(resource).amount

    override fun processResources(pack: ResourcePack, group: Group, parent: Worship) =
            group.resourceCenter.addAll(pack)

    override fun adopt(group: Group) = Fetish(resource)

    override fun swapWorship(worshipObject: WorshipObject) = Fetish(resource)

    override fun toString() = "Fetish of ${resource.fullName}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Fetish) return false

        if (resource != other.resource) return false

        return true
    }

    override fun hashCode() = resource.hashCode()
}
