package simulation.culture.aspect

import simulation.culture.group.centers.Group
import simulation.culture.group.centers.PopulationCenter
import simulation.culture.group.request.ResourceEvaluator
import simulation.culture.thinking.meaning.Meme
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack

data class AspectController(
        val depth: Int,
        var ceiling: Int,
        var floor: Int,
        val evaluator: ResourceEvaluator,
        val populationCenter: PopulationCenter,
        val territory: Territory,
        val isMeaningNeeded: Boolean = false,
        val group: Group,
        val meaning: Meme = group.cultureCenter.meaning
) {
    fun setMax(amount: Int) {
        if (amount < 0) {
            val o = 0
        }
        if (ceiling > amount)
            ceiling = amount
        if (floor > amount)
            floor = amount
    }

    fun evaluate(pack: ResourcePack) = evaluator.evaluate(pack)

    fun isFloorExceeded(resourcePack: MutableResourcePack): Boolean {
        return evaluator.evaluate(resourcePack) >= floor
    }

    fun isCeilingExceeded(resourcePack: ResourcePack): Boolean {
        return evaluator.evaluate(resourcePack) >= ceiling
    }

    fun getCeilingSatisfiableAmount(resources: Collection<Resource>) =
            evaluator.getSatisfiableAmount(ceiling, resources)

    fun pickCeilingPart(
            resources: Collection<Resource>,
            onePortionGetter: (Resource) -> Collection<Resource>,
            partGetter: (Resource, Int) -> Collection<Resource>
    ) = evaluator.pick(
            ceiling,
            resources,
            onePortionGetter,
            partGetter
    )
}