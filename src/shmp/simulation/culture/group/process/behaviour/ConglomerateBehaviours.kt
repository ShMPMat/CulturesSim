package shmp.simulation.culture.group.process.behaviour

import shmp.random.singleton.randomUnwrappedElementOrNull
import shmp.random.toSampleSpaceObject
import shmp.simulation.Controller.session
import shmp.simulation.culture.group.Add
import shmp.simulation.culture.group.centers.AdministrationType
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.culture.group.centers.toPositiveChange
import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.action.GroupTransferA
import shmp.simulation.culture.group.process.action.MakeSplitGroupA
import shmp.simulation.culture.group.process.action.TryDivergeA
import shmp.simulation.culture.group.process.action.pseudo.ActionSequencePA
import shmp.simulation.culture.group.process.emptyProcessResult
import shmp.simulation.culture.group.process.interaction.GroupTransferWithNegotiationI
import shmp.simulation.culture.group.process.interaction.ProbableStrikeWarI
import shmp.simulation.event.Event
import shmp.simulation.event.Type
import kotlin.math.pow
import kotlin.math.sqrt


object RandomGroupSeizureB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val groupValueMapper = { g: Group ->
            val relation = group.relationCenter.getNormalizedRelation(g).pow(3)
            val territoryValue = group.territoryCenter.territoryPotentialMapper(g.territoryCenter.territory).toDouble()
            relation * sqrt(territoryValue)
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
            ).run() +
                    ProcessResult(Event(Type.Change, "${opponent.name} diverged to it's own Conglomerate"))
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

        return ProcessResult(Event(Type.Creation, "${group.name} made a new group ${newGroup.name}")) +
                ProcessResult(Trait.Expansion.toPositiveChange())
    }

    override val internalToString = "Try to split Group in two"
}
