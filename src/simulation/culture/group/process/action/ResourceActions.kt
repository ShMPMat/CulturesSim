package simulation.culture.group.process.action

import simulation.culture.group.centers.Group
import simulation.culture.group.passingReward
import simulation.culture.group.request.Request
import simulation.culture.group.request.RequestCore
import simulation.culture.group.request.SimpleResourceRequest
import simulation.culture.group.request.resourceToRequest
import simulation.space.resource.Resource
import simulation.space.resource.container.ResourcePack
import simulation.space.resource.container.ResourcePromise
import simulation.space.resource.container.ResourcePromisePack
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
        val need: Int
) : AbstractGroupAction(group) {
    override fun run() =
            group.populationCenter.executeRequest(resourceToRequest(resource, group, amount, need)).pack

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
                    RequestCore(group, amount.toDouble(), amount.toDouble(), passingReward, passingReward, need)
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
            "Let ${group.name} choose Resources from $pack in amount of $amount, excluding ${banned.joinToString()} "
}
