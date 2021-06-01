package shmp.simulation.culture.group.request

import shmp.simulation.culture.aspect.AspectController
import shmp.simulation.culture.aspect.hasMeaning
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.stratum.AspectStratum
import shmp.simulation.culture.group.stratum.Stratum
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.ResourcePack

class MeaningResourceRequest(
        private val meme: Meme,
        private val resource: Resource,
        core: RequestCore
) : Request(core) {
    override fun reducedAmountCopy(amount: Double) =
            MeaningResourceRequest(meme, resource, core.copy(floor = 0.0, ceiling = amount))

    override fun isAcceptable(stratum: Stratum) =
            if (stratum is AspectStratum && stratum.aspect.canInsertMeaning)
                super.isAcceptable(stratum)
            else null

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
            meme
    )

    override fun toString() = "want resource ${resource.fullName} with meaning $meme"
}
