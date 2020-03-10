package simulation.culture.aspect

import simulation.culture.group.Group
import simulation.culture.group.request.ResourceEvaluator
import simulation.space.resource.ResourcePack

class AspectController(
        var ceiling: Int,
        var floor: Int,
        val evaluator: ResourceEvaluator,
        val group: Group,
        val isMeaningNeeded: Boolean = false
) {
    fun isFloorExceeded(resourcePack: ResourcePack): Boolean {
        return evaluator.evaluate(resourcePack) >= floor
    }

    fun isCeilingExceeded(resourcePack: ResourcePack): Boolean {
        return evaluator.evaluate(resourcePack) >= ceiling
    }

}