package simulation.culture.aspect

import simulation.culture.group.centers.Group
import simulation.culture.group.centers.PopulationCenter
import simulation.culture.group.request.ResourceEvaluator
import simulation.culture.thinking.meaning.Meme
import simulation.space.Territory
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.container.ResourcePack
import kotlin.math.max


data class AspectController(
        val depth: Int,
        var ceiling: Double,
        var floor: Double,
        val evaluator: ResourceEvaluator,
        val populationCenter: PopulationCenter,
        val territory: Territory,
        val isMeaningNeeded: Boolean = false,
        val group: Group,
        val meaning: Meme = group.cultureCenter.meaning
) {
    init {
        floor = max(0.0, floor)
        ceiling = max(0.0, ceiling)
    }

    fun setMax(amount: Double) {
        if (amount < 0)
            return

        if (ceiling > amount)
            ceiling = amount
        if (floor > amount)
            floor = amount
    }

    fun evaluate(pack: ResourcePack) = evaluator.evaluate(pack)

    fun isFloorExceeded(resourcePack: MutableResourcePack) = evaluator.evaluate(resourcePack) >= floor

    fun isCeilingExceeded(resourcePack: ResourcePack) = evaluator.evaluate(resourcePack) >= ceiling

    fun left(pack: ResourcePack) = max(0.0, ceiling - evaluate(pack))

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
