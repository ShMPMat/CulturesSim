package shmp.simulation.culture.group.process.behaviour

import shmp.random.singleton.chanceOf
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.Trait.Consolidation
import shmp.simulation.culture.group.centers.Trait.values
import shmp.simulation.culture.group.centers.toChange
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.emptyProcessResult
import shmp.utils.without
import kotlin.math.pow


object ConsolidationB : AbstractGroupBehaviour() {
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
                for (trait in values()) {
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