package shmp.simulation.culture.group.cultureaspect.worship

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.ResourceBan
import shmp.simulation.culture.group.centers.ResourceBanProvider
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.space.resource.Resource


class BannedResource(
        val resource: Resource,
        override val allowedTypes: Set<RequestType>
) : BaseWorshipFeature(), ResourceBanProvider {
    init {
        isFunctioning = true
    }

    private var appliedGroups = mutableSetOf<Group>()

    override fun use(group: Group, parent: Worship) {
        if (!appliedGroups.contains(group)) {
            appliedGroups.add(group)

            group.resourceCenter.addBan(resource, this)
        }
    }

    override fun adopt(group: Group) = BannedResource(resource, allowedTypes)

    override fun swapWorship(worshipObject: WorshipObject) = BannedResource(resource, allowedTypes)

    override fun die(group: Group, parent: Worship) {
        if (appliedGroups.contains(group)) {
            group.resourceCenter.removeBan(resource, this)
            appliedGroups.remove(group)
        }
    }

    override fun toString() = "${resource.fullName} is banned" +
            ResourceBan(allowedTypes.toMutableSet(), mutableListOf(this))
}
