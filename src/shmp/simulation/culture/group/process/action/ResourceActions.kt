package shmp.simulation.culture.group.process.action

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.passingReward
import shmp.simulation.culture.group.request.*
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.ResourcePack
import shmp.simulation.space.resource.container.ResourcePromise
import shmp.simulation.space.resource.container.ResourcePromisePack
import kotlin.math.ceil


class ReceiveGroupWideResourcesA(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run() = group.resourceCenter.addAll(pack)

    override val internalToString = "Let main center of ${group.name} receive ${pack.listResources}"
}

class ReceivePopulationResourcesA(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run() = group.populationCenter.turnResources.addAll(pack)

    override val internalToString = "Let population of ${group.name} receive ${pack.listResources}"
}

class ReceiveRequestResourcesA(
        group: Group,
        val request: Request,
        val pack: ResourcePack
) : AbstractGroupAction(group) {
    override fun run() = group.cultureCenter.requestCenter.turnRequests.requests[request]?.addAll(pack)
            ?: ReceivePopulationResourcesA(group, pack).run()

    override val internalToString = "Add ${pack.listResources} to the $request of ${group.name}"
}


class EvaluateResourcesA(group: Group, val pack: ResourcePack) : AbstractGroupAction(group) {
    override fun run() = pack.resources
            .map { group.cultureCenter.evaluateResource(it) }
            .foldRight(0, Int::plus)
            .toDouble()

    override val internalToString = "Let ${group.name} evaluate ${pack.listResources}"
}

class ProduceExactResourceA(
        group: Group,
        val resource: Resource,
        val amount: Int,
        val need: Int,
        val requestTypes: Set<RequestType>
) : AbstractGroupAction(group) {
    override fun run() =
            group.populationCenter.executeRequest(resourceToRequest(resource, group, amount, need, requestTypes)).pack

    override val internalToString = "Get ${resource.fullName} in amount of $amount from ${group.name}, need is $need"
}

class ExecuteRequestA(
        group: Group,
        val request: Request
) : AbstractGroupAction(group) {
    override fun run() = group.populationCenter.executeRequest(request).pack

    override val internalToString = "Get executed $request from ${group.name}"
}

class ProduceSimpleResourceA(
        group: Group,
        val resource: Resource,
        val amount: Int,
        val need: Int
) : AbstractGroupAction(group) {
    override fun run() =
            group.populationCenter.executeRequest(SimpleResourceRequest(
                    resource,
                    RequestCore(group, amount.toDouble(), amount.toDouble(), passingReward, passingReward, need, setOf())
            )).pack

    override val internalToString =
            "Get a Resource similar to ${resource.fullName} in amount of $amount from the ${group.name}, need is $need"
}

class ChooseResourcesA(
        group: Group,
        val pack: ResourcePromisePack,
        val amount: Int,
        val banned: List<Resource> = listOf()
) : AbstractGroupAction(group) {
    override fun run(): ResourcePromisePack {
        val chosenResources = mutableListOf<ResourcePromise>()
        var leftAmount = amount
        val sortedResources = pack.resources
                .filter { p -> p.resource.genome.isMovable && p.resource !in banned }

        for (promise in sortedResources) {
            val worth = group.cultureCenter.evaluateResource(promise.makeCopy())

            val amountToTook = ceil(leftAmount.toDouble() / worth).toInt()
            val takenPart = ResourcePromise(promise.resource, amountToTook)
            leftAmount -= takenPart.amount * worth
            chosenResources.add(takenPart)

            if (leftAmount <= 0) break
        }

        return ResourcePromisePack(chosenResources)
    }

    override val internalToString =
            "Let ${group.name} choose Resources from $pack in amount of $amount, excluding ${banned.joinToString()}"
}


class ChooseResourcesAndTakeA(
        group: Group,
        val pack: ResourcePromisePack,
        val amount: Int
) : AbstractGroupAction(group) {
    override fun run() {
        val chosenResources = ChooseResourcesA(group, pack, amount).run().extract(group.populationCenter.taker)

        ReceivePopulationResourcesA(group, chosenResources).run()
    }

    override val internalToString =
            "Let ${group.name} choose Resources from $pack in amount of $amount, and take it"
}
