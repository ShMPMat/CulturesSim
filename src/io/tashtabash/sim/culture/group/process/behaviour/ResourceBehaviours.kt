package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.random.singleton.randomUnwrappedElementOrNull
import io.tashtabash.random.withProb
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.culture.group.process.interaction.GiveGiftI


object GiveGiftB: AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val relatedGroups = group.relationCenter.relatedGroups
                .map { it.withProb(group.relationCenter.getNormalizedRelation(group)) }
                .filter { it.probability > 0 }

        val receiver = relatedGroups.randomUnwrappedElementOrNull()
                ?: return emptyProcessResult

        return GiveGiftI(group, receiver).run()
    }

    override val internalToString = "Give a gift to a random Group"
}
