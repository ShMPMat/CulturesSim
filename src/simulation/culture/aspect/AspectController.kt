package simulation.culture.aspect

import simulation.culture.group.PopulationCenter
import simulation.culture.group.request.ResourceEvaluator
import simulation.culture.thinking.meaning.Meme
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.ResourcePack

data class AspectController(
        var ceiling: Int,
        var floor: Int,
        val evaluator: ResourceEvaluator,
        val populationCenter: PopulationCenter,
        val territory: Territory,
        val isMeaningNeeded: Boolean = false,
        val meaning: Meme?
) {
    fun isFloorExceeded(resourcePack: MutableResourcePack): Boolean {
        return evaluator.evaluate(resourcePack) >= floor
    }

    fun isCeilingExceeded(resourcePack: ResourcePack): Boolean {
        return evaluator.evaluate(resourcePack) >= ceiling
    }

}