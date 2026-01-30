package io.tashtabash.sim.culture.group.cultureaspect.worship

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.container.ResourcePack


class Fetish(resource: Resource) : WorshipResourceFeature(resource) {
    override fun doUse(group: Group, parent: Worship) = true

    override fun getNeededAmount(group: Group, parent: Worship) = group.populationCenter.amount -
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
