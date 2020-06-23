package simulation.culture.group.request

import simulation.culture.aspect.AspectController
import simulation.culture.aspect.hasMeaning
import simulation.culture.group.centers.Group
import simulation.culture.group.stratum.AspectStratum
import simulation.culture.group.stratum.Stratum
import simulation.culture.thinking.meaning.Meme
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack

class MeaningResourceRequest(
        group: Group,
        private val meme: Meme,
        private val resource: Resource,
        floor: Int,
        ceiling: Int,
        penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        reward: (Pair<Group, MutableResourcePack>, Double) -> Unit
) : Request(group, floor, ceiling, penalty, reward) {
    override fun reducedAmountCopy(amount: Int) =
            MeaningResourceRequest(group, meme, resource, 0, amount, penalty, reward)

    override fun isAcceptable(stratum: Stratum) =
            if (stratum is AspectStratum && stratum.aspect.canInsertMeaning)
                super.isAcceptable(stratum)
            else null

    override val evaluator = resourceEvaluator(resource)

    override fun reassign(group: Group) =
            MeaningResourceRequest(group, meme, resource, floor, ceiling, penalty, reward)

    override fun getController(ignoreAmount: Int) = AspectController(
            1,
            ceiling - ignoreAmount,
            floor - ignoreAmount,
            evaluator,
            group.populationCenter,
            group.territoryCenter.accessibleTerritory,
            true,
            group,
            meme
    )

    override fun finalFilter(pack: MutableResourcePack): ResourcePack {
        val result = pack.getResources { evaluator.evaluate(it) > 0 && it.hasMeaning }
        pack.removeAll(result)
        return result
    }

    override fun toString() = "want resource ${resource.fullName} with meaning $meme"
}