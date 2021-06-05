package shmp.simulation.culture.aspect

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.PopulationCenter
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.ResourceEvaluator
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.ResourcePack
import kotlin.math.max


data class AspectController constructor(
        val depth: Int,
        var ceiling: Double,
        var floor: Double,
        val evaluator: ResourceEvaluator,
        val populationCenter: PopulationCenter,
        val territory: Territory,
        val isMeaningNeeded: Boolean = false,
        val group: Group,
        val requestTypes: Set<RequestType>,
        val meaning: Meme? = null
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
            onePortionGetter: (Resource) -> List<Resource>,
            partGetter: (Resource, Int) -> List<Resource>
    ) = evaluator.pick(
            ceiling,
            resources,
            onePortionGetter,
            partGetter
    )
}
