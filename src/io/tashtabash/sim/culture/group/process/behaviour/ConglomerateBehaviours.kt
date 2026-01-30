package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.random.singleton.randomUnwrappedElementOrNull
import io.tashtabash.random.toSampleSpaceObject
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.culture.group.Add
import io.tashtabash.sim.culture.group.centers.AdministrationType
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.centers.toPositiveChange
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.action.GroupTransferA
import io.tashtabash.sim.culture.group.process.action.MakeSplitGroupA
import io.tashtabash.sim.culture.group.process.action.TryDivergeA
import io.tashtabash.sim.culture.group.process.action.pseudo.ActionSequencePA
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.culture.group.process.interaction.GroupTransferWithNegotiationI
import io.tashtabash.sim.culture.group.process.interaction.ProbableStrikeWarI
import io.tashtabash.sim.event.Change
import io.tashtabash.sim.event.Creation
import io.tashtabash.sim.event.Event
import io.tashtabash.sim.event.of
import kotlin.math.pow
import kotlin.math.sqrt


object RandomGroupSeizureB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val groupValueMapper = { g: Group ->
            val relation = group.relationCenter.getNormalizedRelation(g).pow(3)
            val territoryValue = group.territoryCenter.territoryPotentialMapper(g.territoryCenter.territory)
            relation * sqrt(territoryValue.toDouble())
        }

        val options = group.territoryCenter.getAllNearGroups(group)
                .filter { it.parentGroup !== group.parentGroup }
                .map { it.toSampleSpaceObject(groupValueMapper(it)) }
                .filter { it.probability > 0 }

        return options.randomUnwrappedElementOrNull()
                ?.let { target ->
                    GroupTransferWithNegotiationI(group, target).run() +
                            ProcessResult(Trait.Expansion.toPositiveChange())
                } ?: ProcessResult(Trait.Expansion.toPositiveChange())
    }

    override val internalToString = "Choose a random Neighbour and add it to the Conglomerate"
}

object TryDivergeWithNegotiationB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val initialConglomerate = group.parentGroup
        val conglomerate = TryDivergeA(group).run()

        return if (conglomerate != null) {
            val initiator = initialConglomerate.subgroups.firstOrNull { it.processCenter.type == AdministrationType.Main }
                    ?: initialConglomerate.subgroups[0]
            val opponent = conglomerate.subgroups.firstOrNull { it.processCenter.type == AdministrationType.Main }
                    ?: conglomerate.subgroups[0]

            ProbableStrikeWarI(
                    initiator,
                    opponent,
                    "${initiator.name} objects ${opponent.name} leaving the Conglomerate",
                    ActionSequencePA(conglomerate.subgroups.map { GroupTransferA(initiator, it) })
            ).reverseRun() +
                    ProcessResult(Change of "${opponent.name} diverged to its own Conglomerate")
        } else
            ProcessResult(Trait.Consolidation.toPositiveChange())
    }

    override val internalToString = "Try to diverge and make Group's own Conglomerate"
}

object SplitGroupB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        if (!session.groupMultiplication)
            return emptyProcessResult

        val tiles = group.overallTerritory.filterOuterBrink {
            group.territoryCenter.canSettleAndNoGroup(it) && group.parentGroup.getClosestInnerGroupDistance(it) > 2
        }
        if (tiles.isEmpty())
            return emptyProcessResult

        val tile = tiles.sortedBy { group.territoryCenter.tilePotentialMapper(it) }[0]
        val newGroup = MakeSplitGroupA(group, tile).run()

        Add(newGroup).execute(group.parentGroup)

        return ProcessResult(Creation of "${group.name} made a new group ${newGroup.name}") +
                ProcessResult(Trait.Expansion.toPositiveChange())
    }

    override val internalToString = "Try to split Group in two"
}
