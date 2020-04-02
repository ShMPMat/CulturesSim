package simulation.culture.group.request

import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack

/**
 * Class which evaluates ResourcePacks for containing needed Resources.
 */
class ResourceEvaluator(
        private val picker: (ResourcePack) -> ResourcePack,
        private val evaluator: (ResourcePack) -> Int
) {
    fun pick(resourcePack: ResourcePack): ResourcePack {
        return picker(resourcePack)
    }

    fun evaluate(resourcePack: ResourcePack): Int {
        return evaluator(resourcePack)
    }

    fun getSatisfiableAmount(part: Int, resources: Collection<Resource>): Int {
        var oneResourceWorth = evaluate(ResourcePack(resources))
        if (oneResourceWorth == 0)
            oneResourceWorth = 1//TODO meh
        return part / oneResourceWorth + if (part % oneResourceWorth == 0) 0 else 1
    }

    fun pick(
            part: Int,
            resources: Collection<Resource>,
            onePortionGetter: (Resource) -> Collection<Resource>,
            partGetter: (Resource, Int) -> Collection<Resource>
    ): MutableResourcePack {
        val resultPack = MutableResourcePack()
        var amount = 0
        for (resource in resources) {
            val neededAmount = getSatisfiableAmount(part, onePortionGetter(resource)) - amount
            resultPack.addAll(partGetter(resource, neededAmount))
            amount = evaluate(resultPack)
            if (amount >= part) break
        }
        return resultPack
    }
}