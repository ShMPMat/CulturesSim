package simulation.culture.group.request

import simulation.SimulationException
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.container.ResourcePack
import simulation.space.resource.tag.labeler.ResourceLabeler
import kotlin.math.ceil


//Evaluates ResourcePacks for containing needed Resources.
// If labeler returns true on a Resource, evaluator MUST return Double > 0.
class ResourceEvaluator(val labeler: ResourceLabeler, private val evaluator: (Resource) -> Double) {
    fun pick(resourcePack: ResourcePack) = resourcePack.getResources { evaluator(it) > 0 }

    fun pickAndRemove(pack: MutableResourcePack) : ResourcePack {
        val result = pick(pack)
        pack.removeAll(result)
        return result
    }

    fun evaluate(resources: Collection<Resource>) = resources
            .map { evaluator(it) }
            .foldRight(0.0, Double::plus)

    fun evaluate(pack: ResourcePack) = evaluate(pack.resources)

    fun evaluate(resource: Resource) = evaluator(resource)

    fun getSatisfiableAmount(part: Double, resources: Collection<Resource>): Double {
        val oneResourceWorth = evaluate(resources)
        return if (oneResourceWorth != 0.0)
            part / oneResourceWorth
        else
            0.0
    }

    fun pick(
            part: Double,
            resources: Collection<Resource>,
            onePortionGetter: (Resource) -> Collection<Resource>,
            partGetter: (Resource, Int) -> Collection<Resource>
    ): MutableResourcePack {
        val resultPack = MutableResourcePack()
        if (part == 0.0)
            return resultPack
        var amount = 0.0
        for (resource in resources) {
            val neededAmount = getSatisfiableAmount(part - amount.toInt(), onePortionGetter(resource))
            if (neededAmount == 0.0)
                continue
            if (neededAmount <= 0)
                throw SimulationException("Wrong needed amount - $neededAmount")

            resultPack.addAll(partGetter(resource, ceil(neededAmount).toInt()))
            amount = evaluate(resultPack)
            if (amount >= part) break
        }
        return resultPack
    }
}
