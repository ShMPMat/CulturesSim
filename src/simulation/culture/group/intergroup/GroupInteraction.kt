package simulation.culture.group.intergroup

import simulation.Event
import simulation.culture.group.centers.Group

interface GroupInteraction {
    val initiator: Group
    val participator: Group

    fun run(): Event?
}

sealed class AbstractGroupInteraction(
        override val initiator: Group,
        override val participator: Group
) : GroupInteraction

class RelationsImprovementInteraction(
        initiator: Group,
        participator: Group,
        val amount: Double
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): Event {
        ImproveRelationsAction(participator, initiator, amount)
        ImproveRelationsAction(initiator, participator, amount)

        val relationTo = initiator.relationCenter.getNormalizedRelation(participator)
        val relationFrom = participator.relationCenter.getNormalizedRelation(initiator)
        return Event(
                Event.Type.GroupInteraction,
                "Groups ${initiator.name} and ${participator.name} improved their relations by $amount " +
                        "to the general of $relationTo and $relationFrom"
        )
    }
}

class TradeInteraction(
        initiator: Group,
        participator: Group,
        val amount: Int
) : AbstractGroupInteraction(initiator, participator) {
    override fun run(): Event? {
        val wantedResources = ChooseResourcesAction(
                initiator,
                participator.populationCenter.turnResources,
                amount
        ).run()
        val price = EvaluateResourcesAction(participator, wantedResources.makeCopy()).run()
        if (price == 0) return null

        val priceInResources = ChooseResourcesAction(
                participator,
                initiator.populationCenter.turnResources,
                price,
                wantedResources.makeCopy().resources
        ).run()

        if (price <= EvaluateResourcesAction(participator, priceInResources.makeCopy()).run()) {
            val got = wantedResources.extract()
            val given = priceInResources.extract()
            val event = Event(
                    Event.Type.GroupInteraction,
                    "Groups ${initiator.name} and ${participator.name} traded $got and $given"
            )
            ReceiveResourcesAction(initiator, got).run()
            ReceiveResourcesAction(participator, given).run()

            RelationsImprovementInteraction(initiator, participator, 0.001).run()
            return event
        }
        return null
    }
}

class GroupTransferInteraction(
        initiator: Group,
        participator: Group
): AbstractGroupInteraction(initiator, participator) {
    override fun run(): Event {
        AddGroupAction(initiator, participator).run()
        ProcessGroupRemovalAction(participator, participator).run()
        return Event(
                Event.Type.GroupInteraction,
                "Group ${participator.name} joined to conglomerate ${initiator.parentGroup.name}"
        )
    }
}
