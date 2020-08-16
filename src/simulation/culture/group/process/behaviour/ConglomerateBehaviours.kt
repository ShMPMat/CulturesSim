package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller.session
import simulation.culture.group.Add
import simulation.culture.group.centers.AdministrationType
import simulation.culture.group.centers.Group
import simulation.culture.group.centers.Trait
import simulation.culture.group.centers.makePositiveChange
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.action.GroupTransferA
import simulation.culture.group.process.action.MakeSplitGroupA
import simulation.culture.group.process.action.TryDivergeA
import simulation.culture.group.process.action.pseudo.ActionSequencePA
import simulation.culture.group.process.emptyProcessResult
import simulation.culture.group.process.interaction.GroupTransferWithNegotiationI
import simulation.culture.group.process.interaction.ProbableStrikeWarI
import simulation.event.Event
import simulation.event.Type
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
                .map { it to groupValueMapper(it) }
                .filter { (_, n) -> n > 0 }

        if (options.isNotEmpty()) {
            val target = randomElement(options, { (_, n) -> n }, session.random).first

            return GroupTransferWithNegotiationI(group, target).run() +
                    ProcessResult(makePositiveChange(Trait.Expansion))
        }
        return ProcessResult(makePositiveChange(Trait.Expansion))
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
            ProcessResult(makePositiveChange(Trait.Consolidation))
    }

    override val internalToString = "Try to diverge and make Group's own Conglomerate"
}

object SplitGroupB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        if (!session.groupMultiplication)
            return emptyProcessResult

        val tiles = group.overallTerritory.getOuterBrink {
            group.territoryCenter.canSettleAndNoGroup(it) && group.parentGroup.getClosestInnerGroupDistance(it) > 2
        }
        if (tiles.isEmpty())
            return emptyProcessResult

        val tile = tiles.sortedBy { group.territoryCenter.tilePotentialMapper(it) }[0]
        val newGroup = MakeSplitGroupA(group, tile).run()

        Add(newGroup).execute(group.parentGroup)

        return ProcessResult(Event(Type.Creation, "${group.name} made a new group ${newGroup.name}")) +
                ProcessResult(makePositiveChange(Trait.Expansion))
    }

    override val internalToString = "Try to split Group in two"
}
