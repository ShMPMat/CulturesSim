package io.tashtabash.simulation.culture.aspect

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.centers.PopulationCenter
import io.tashtabash.simulation.culture.group.request.RequestType
import io.tashtabash.simulation.culture.group.request.ResourceEvaluator
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.simulation.space.territory.Territory
import io.tashtabash.simulation.space.resource.container.MutableResourcePack
import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.container.ResourcePack
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

    fun evaluate(pack: ResourcePack) = evaluator.evaluatePack(pack)

    fun isFloorExceeded(resourcePack: MutableResourcePack) = evaluator.evaluatePack(resourcePack) >= floor

    fun isCeilingExceeded(resourcePack: ResourcePack) = evaluator.evaluatePack(resourcePack) >= ceiling

    fun isCeilingExceeded(amount: Double) = amount >= ceiling
    fun isCeilingExceeded(amount: Int) = amount >= ceiling

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
