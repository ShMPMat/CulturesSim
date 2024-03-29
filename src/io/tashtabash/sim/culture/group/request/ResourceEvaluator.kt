package io.tashtabash.sim.culture.group.request

import io.tashtabash.sim.SimulationError
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.container.ResourcePack
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler
import kotlin.math.ceil


//Evaluates ResourcePacks for containing needed Resources.
// If labeler returns true on a Resource, evaluator MUST return Double > 0.
// Evaluator MUST be linear
class ResourceEvaluator(val labeler: ResourceLabeler, private val evaluator: (Resource) -> Double) {
    fun pick(resourcePack: ResourcePack) = resourcePack.getResources { evaluator(it) > 0 }
    fun pick(resources: List<Resource>) = resources.filter { evaluator(it) > 0 }

    fun pickAndRemove(pack: MutableResourcePack) : ResourcePack {
        val result = pick(pack)
        pack.removeAll(result)
        return result
    }

    fun pickAndRemove(pack: MutableList<Resource>) : List<Resource> {
        val result = pick(pack)
        pack.removeAll(result)
        return result
    }

    fun hasValue(resources: List<Resource>) = resources.any { evaluator(it) > 0 }

    fun evaluate(resources: List<Resource>) = resources.sumOf { evaluator(it) }

    fun evaluatePack(pack: ResourcePack) = evaluate(pack.resources)

    fun evaluate(resource: Resource) = evaluator(resource)

    fun getSatisfiableAmount(part: Double, resources: List<Resource>): Double {
        val oneResourceWorth = evaluate(resources)
        return if (oneResourceWorth != 0.0)
            part / oneResourceWorth
        else 0.0
    }

    fun pick(
            part: Double,
            resources: Collection<Resource>,
            onePortionGetter: (Resource) -> List<Resource>,
            partGetter: (Resource, Int) -> List<Resource>
    ): MutableList<Resource> {
        val resultPack = mutableListOf<Resource>()
        if (part == 0.0)
            return resultPack
        var amount = 0.0

        for (resource in resources) {
            val neededAmount = getSatisfiableAmount(part - amount.toInt(), onePortionGetter(resource))
            if (neededAmount == 0.0)
                continue
            if (neededAmount <= 0)
                throw SimulationError("Wrong needed amount - $neededAmount")

            val newResources = partGetter(resource, ceil(neededAmount).toInt())
            amount += evaluate(newResources)
            resultPack.addAll(newResources)

            if (amount >= part)
                break
        }
        return resultPack
    }
}
