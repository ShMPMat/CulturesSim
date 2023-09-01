package io.tashtabash.sim.culture.group.cultureaspect.worship

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.request.RequestType
import io.tashtabash.sim.culture.group.request.resourceToRequest
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.container.ResourcePack


abstract class WorshipResourceFeature(val resource: Resource) : BaseWorshipFeature() {
    override fun use(group: Group, parent: Worship) {
        super.use(group, parent)

        if (!doUse(group, parent))
            return

        var diff = getNeededAmount(group, parent)

        if (diff > 0) {
            isFunctioning = false
            val request = resourceToRequest(resource, group, diff, diff, setOf(RequestType.Spiritual))
            val result = group.populationCenter.executeRequest(request)

            val gotten = result.pack
            diff -= gotten.amount

            if (diff > 0) {
                isFunctioning = true

                group.resourceCenter.addNeeded(request.evaluator.labeler, diff * 5)
            }

            processResources(gotten, group, parent)
        } else isFunctioning = true
    }

    protected abstract fun doUse(group: Group, parent: Worship): Boolean
    protected abstract fun getNeededAmount(group: Group, parent: Worship): Int
    protected abstract fun processResources(pack: ResourcePack, group: Group, parent: Worship)

    override fun die(group: Group, parent: Worship) = Unit
}
