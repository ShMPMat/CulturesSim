package simulation.culture.group.intergroup

import simulation.culture.group.centers.Group
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.container.ResourcePack
import kotlin.math.ceil

interface GroupAction {
    val group: Group

    fun run(): Any
}

sealed class AbstractGroupAction(override val group: Group) : GroupAction

class ReceiveResourcesAction(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run() = group.resourceCenter.addAll(pack)
}

class ImproveRelationsAction(group: Group, val target: Group, val amount: Double) : AbstractGroupAction(group) {
    override fun run() {
        val targetRelation = group.relationCenter.relations.firstOrNull { it.other == target }
        if (targetRelation != null)
            targetRelation.positive += amount
    }
}

class EvaluateResourcesAction(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run() = pack.resources
            .map { group.cultureCenter.evaluateResource(it) }
            .foldRight(0, Int::plus)
}

class ChooseResourcesAction(group: Group, val pack: ResourcePack, val amount: Int) : AbstractGroupAction(group) {
    override fun run(): ResourcePack {
        val chosenResources = MutableResourcePack()
        var leftAmount = amount
        val sortedResources = pack.resources.sortedByDescending { group.cultureCenter.evaluateResource(it) }

        for (resource in sortedResources) {
            val worth = group.cultureCenter.evaluateResource(resource)
            val amountToTook = ceil(leftAmount.toDouble() / worth).toInt()

            val takenPart = resource.getCleanPart(amountToTook)
            leftAmount -= takenPart.amount
            chosenResources.add(takenPart)

            if (leftAmount <= 0) break
        }

        return chosenResources
    }
}
