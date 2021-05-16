package shmp.simulation.culture.aspect

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler
import java.util.*
import java.util.function.Consumer


data class AspectResult(
        var resources: MutableResourcePack = MutableResourcePack(),
        var node: ResultNode? = null,
        var isFinished: Boolean = true,
        var neededResources: List<Pair<ResourceLabeler, Int>> = emptyList()
) {
    fun pushNeeds(group: Group) = neededResources.forEach { (labeler, n) ->
        group.resourceCenter.addNeeded(labeler, n)
    }
}


data class ResultNode(var aspect: Aspect) {
    var resourceUsed: MutableMap<ResourceTag, MutableResourcePack> = HashMap()
}
