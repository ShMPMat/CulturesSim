package simulation.culture.group.intergroup

import simulation.culture.group.Transfer
import simulation.culture.group.centers.Group
import simulation.culture.group.request.resourceToRequest
import simulation.culture.group.stratum.Stratum
import simulation.culture.group.stratum.TraderStratum
import simulation.space.resource.Resource
import simulation.space.resource.container.ResourcePack
import simulation.space.resource.container.ResourcePromise
import simulation.space.resource.container.ResourcePromisePack
import kotlin.math.ceil
import kotlin.math.log

interface GroupAction {
    val group: Group

    fun run(): Any
}

sealed class AbstractGroupAction(override val group: Group) : GroupAction

class ReceiveGroupWideResourcesA(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run() = group.resourceCenter.addAll(pack)
}


class ReceivePopulationResourcesA(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run() = group.populationCenter.turnResources.addAll(pack)
}

class ImproveRelationsA(group: Group, val target: Group, val amount: Double) : AbstractGroupAction(group) {
    override fun run() {
        val targetRelation = group.relationCenter.relations.firstOrNull { it.other == target }
        if (targetRelation != null)
            targetRelation.positive += amount
    }
}

class IncStratumImportanceA(group: Group, val stratum: Stratum, val amount: Int) : AbstractGroupAction(group) {
    override fun run() {
        stratum.importance += amount
    }
}

class EvaluateResourcesA(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run() = pack.resources
            .map { group.cultureCenter.evaluateResource(it) }
            .foldRight(0, Int::plus)
}

class TradeEvaluateResourcesA(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run(): Int {
        val traderSkill = group.populationCenter.strata
                .filterIsInstance<TraderStratum>()
                .firstOrNull()
                ?.effectiveness
                ?: 1.0
        return EvaluateResourcesA(group, pack).run() / log(traderSkill + 1.0, 2.0).toInt()
    }
}

class ProduceResourceA(
        group: Group,
        val resource: Resource,
        val amount: Int,
        val need: Int
): AbstractGroupAction(group) {
    override fun run() =
            group.populationCenter.executeRequest(resourceToRequest(resource, group, amount, need)).pack
}

class ChooseResourcesA(
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

class AddGroupA(group: Group, val groupToAdd: Group): AbstractGroupAction(group) {
    override fun run() {
        Transfer(groupToAdd).execute(group.parentGroup)
    }
}

class ProcessGroupRemovalA(group: Group, val groupToRemove: Group): AbstractGroupAction(group) {
    override fun run() {

    }
}
