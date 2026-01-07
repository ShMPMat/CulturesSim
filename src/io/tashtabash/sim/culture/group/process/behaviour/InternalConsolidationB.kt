package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.centers.Trait.Consolidation
import io.tashtabash.sim.culture.group.centers.toChange
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.utils.without
import kotlin.math.pow


object InternalConsolidationB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val result = emptyProcessResult
        val traitCenter = group.cultureCenter.traitCenter
        val groupConsolidation = traitCenter.value(Consolidation)

        val groups = group.parentGroup.subgroups.without(group)
        for (otherGroup in groups) {
            val otherTraitCenter = otherGroup.cultureCenter.traitCenter
            val otherGroupConsolidation = otherTraitCenter.value(Consolidation)
            val consolidationBond = groupConsolidation + otherGroupConsolidation

            (1 + consolidationBond.value).pow(2).chanceOf {
                for (trait in Trait.entries) {
                    val diff = traitCenter.value(trait) - otherTraitCenter.value(trait)

                    traitCenter.changeOn(trait.toChange(-diff / 2.0 * groupConsolidation.positive))
                    otherTraitCenter.changeOn(trait.toChange(diff / 2.0 * consolidationBond.norm))
                }
            }

        }

        return result
    }

    override val internalToString = "Try to consolidate the Conglomerate more"
}