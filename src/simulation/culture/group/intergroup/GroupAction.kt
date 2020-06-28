package simulation.culture.group.intergroup

import simulation.culture.group.centers.Group
import simulation.space.resource.Resource
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.container.ResourcePack
import simulation.space.resource.container.ResourcePromise
import simulation.space.resource.container.ResourcePromisePack
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

class ChooseResourcesAction(
        group: Group,
        val pack: ResourcePack,
        val amount: Int,
        val banned: List<Resource> = listOf()
) : AbstractGroupAction(group) {
    override fun run(): ResourcePromisePack {
        val chosenResources = mutableListOf<ResourcePromise>()
        var leftAmount = amount
        val sortedResources = pack.resources
                .filter { it.genome.isMovable }
                .sortedByDescending { group.cultureCenter.evaluateResource(it) }

        for (resource in sortedResources) {
            val worth = group.cultureCenter.evaluateResource(resource)
            if(worth <= 1)
                break
            if (banned.any { it.ownershiplessEquals(resource) })
                continue

            val amountToTook = ceil(leftAmount.toDouble() / worth).toInt()
            val takenPart = ResourcePromise(resource, amountToTook)
            leftAmount -= takenPart.amount * worth
            chosenResources.add(takenPart)

            if (leftAmount <= 0) break
        }

        return ResourcePromisePack(chosenResources)
    }
}
