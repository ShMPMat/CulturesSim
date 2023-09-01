package io.tashtabash.sim.culture.aspect

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler
import java.util.*


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
