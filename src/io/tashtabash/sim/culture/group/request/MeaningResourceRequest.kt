package io.tashtabash.sim.culture.group.request

import io.tashtabash.sim.culture.aspect.AspectController
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.stratum.AspectStratum
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.sim.space.resource.Resource


class MeaningResourceRequest(
        private val meme: Meme,
        private val resource: Resource,
        core: RequestCore
) : Request(core) {
    override fun reducedAmountCopy(amount: Double) =
            MeaningResourceRequest(meme, resource, core.copy(floor = 0.0, ceiling = amount))

    override fun isAcceptable(stratum: AspectStratum) =
            if (stratum.aspect.canInsertMeaning)
                super.isAcceptable(stratum)
            else false

    override val evaluator = resourceEvaluator(resource)

    override fun reassign(group: Group) =
            MeaningResourceRequest(meme, resource, core.copy(group = group))

    override fun getController(ignoreAmount: Int) = AspectController(
            1,
            core.ceiling - ignoreAmount,
            core.floor - ignoreAmount,
            evaluator,
            core.group.populationCenter,
            core.group.territoryCenter.accessibleTerritory,
            true,
            core.group,
            types,
            meme,
    )

    override fun toString() = "want resource ${resource.fullName} with meaning $meme"
}
