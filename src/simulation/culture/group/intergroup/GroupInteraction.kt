package simulation.culture.group.intergroup

import simulation.culture.group.centers.Group

interface GroupInteraction {
    val initiator: Group
    val participator: Group

    fun run()
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
    override fun run() {
        ImproveRelationsAction(participator, initiator, amount)
        ImproveRelationsAction(initiator, participator, amount)
    }
}

class TradeInteraction(
        initiator: Group,
        participator: Group,
        val amount: Int
) : AbstractGroupInteraction(initiator, participator) {
    override fun run() {//TODO special class for pre-split Resources
        val wantedResources = ChooseResourcesAction(
                initiator,
                participator.resourceCenter.pack,
                amount
        ).run()
        val price = EvaluateResourcesAction(participator, wantedResources.makeCopy()).run()
        if (price == 0) return

        val priceInResources = ChooseResourcesAction(
                participator,
                initiator.resourceCenter.pack,
                price,
                wantedResources.makeCopy().resources
        ).run()

        if (price <= EvaluateResourcesAction(participator, priceInResources.makeCopy()).run()) {
            val got = wantedResources.extract()
            val given = priceInResources.extract()
            ReceiveResourcesAction(initiator, got)
            ReceiveResourcesAction(participator, given)

            RelationsImprovementInteraction(initiator, participator, 0.001)
        }
    }
}
