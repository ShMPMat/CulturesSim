package shmp.simulation.culture.group.cultureaspect.worship

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.resourceToRequest
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.ResourcePack


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
