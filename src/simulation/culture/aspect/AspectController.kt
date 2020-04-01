package simulation.culture.aspect

import simulation.culture.group.PopulationCenter
import simulation.culture.group.request.ResourceEvaluator
import simulation.culture.thinking.meaning.Meme
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
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

    fun getCeilingSatisfiableAmount(resources: Collection<Resource>): Int {
        var oneResourceWorth = evaluator.evaluate(ResourcePack(resources))
        if (oneResourceWorth == 0)
            oneResourceWorth = 1//TODO meh
        return ceiling / oneResourceWorth + if (ceiling % oneResourceWorth == 0) 0 else 1
    }

    fun pick(
            pack: ResourcePack,
            onePortionGetter: (Resource) -> Collection<Resource>,
            partGetter: (Resource, Int) -> Collection<Resource>
    ): MutableResourcePack {
        val resultPack = MutableResourcePack()
        var amount = 0
        for (resource in pack.resources) {
            val neededAmount = getCeilingSatisfiableAmount(onePortionGetter(resource)) - amount
            resultPack.addAll(partGetter(resource, neededAmount))
            amount = evaluator.evaluate(resultPack)
            if (amount >= ceiling) break
        }
        return resultPack
    }
}