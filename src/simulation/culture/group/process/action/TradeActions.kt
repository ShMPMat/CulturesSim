package simulation.culture.group.process.action

import simulation.culture.group.centers.Group
import simulation.culture.group.stratum.TraderStratum
import simulation.space.resource.container.ResourcePack
import kotlin.math.log


class TradeEvaluateResourcesA(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run(): Int {
        val traderSkill = group.populationCenter.strata
                .filterIsInstance<TraderStratum>()
                .firstOrNull()
                ?.effectiveness
                ?: 1.0
        return EvaluateResourcesA(group, pack).run() / log(traderSkill + 1.0, 2.0).toInt()
    }
}