package simulation.culture.group.request

import simulation.culture.aspect.AspectController
import simulation.culture.aspect.hasMeaning
import simulation.culture.group.centers.Group
import simulation.culture.group.stratum.AspectStratum
import simulation.culture.group.stratum.Stratum
import simulation.culture.thinking.meaning.Meme
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.container.ResourcePack

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

    override fun finalFilter(pack: MutableResourcePack): ResourcePack {
        val result = pack.getResources { evaluator.evaluate(it) > 0 && it.hasMeaning }
        pack.removeAll(result)
        return result
    }

    override fun toString() = "want resource ${resource.fullName} with meaning $meme"
}
