package io.tashtabash.sim.culture.group.centers

import io.tashtabash.sim.culture.clothesTag
import io.tashtabash.sim.culture.foodTag
import io.tashtabash.sim.culture.group.*
import io.tashtabash.sim.culture.group.request.*
import io.tashtabash.sim.culture.group.request.RequestType.*
import io.tashtabash.sim.culture.shelterTag
import io.tashtabash.sim.culture.warmthTag
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.resource.container.ResourcePack
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.labeler.TagLabeler
import java.util.*
import kotlin.math.max


class RequestCenter {
    private var _unfinishedRequestMap = mutableMapOf<Request, MutableResourcePack>()

    var turnRequests = RequestPool(mapOf())
        private set

    private val nerfCoefficients = mutableMapOf<Request, Int>()

    private fun getRequestNerfCoefficient(request: Request, amount: Double): Double {
        val coefficient = max(
                nerfCoefficients[request] ?: 1,
                1
        ).toDouble()
        return (amount / coefficient).coerceAtLeast(1.0)
    }

    internal fun updateRequests(group: Group) {
        turnRequests = constructRequests(RequestConstructController(
                group,
                group.populationCenter.population,
                group.resourceCenter.pack,
                turnRequests
        ))
    }

    fun constructRequests(controller: RequestConstructController): RequestPool {
        _unfinishedRequestMap = mutableMapOf()

        addFoodRequest(controller)

        addWarmthRequest(controller)

        val foodRequestDummy = constructFoodRequest(controller)
        if (controller.previous.resultStatus.containsKey(foodRequestDummy) &&
                controller.previous.resultStatus[foodRequestDummy]?.status !== ResultStatus.NotSatisfied) {
            addClothesRequest(controller)
            addShelterRequest(controller)
        }

        controller.group.cultureCenter.cultureAspectCenter.aspectPool.getAspectRequests(controller.group)
                .filter { Objects.nonNull(it) }
                .forEach { _unfinishedRequestMap[it] = MutableResourcePack() }

        return RequestPool(_unfinishedRequestMap)
    }

    private fun addFoodRequest(controller: RequestConstructController) {
        _unfinishedRequestMap[constructFoodRequest(controller)] = MutableResourcePack()
    }

    private fun addWarmthRequest(controller: RequestConstructController) =
            controller.group.territoryCenter.territory.minTemperature
                    ?.takeIf { it < 0 }
                    ?.let {
                        _unfinishedRequestMap[constructWarmthRequest(controller)] = MutableResourcePack()
                    }

    private fun addClothesRequest(controller: RequestConstructController) {
        val clothesEvaluator = tagEvaluator(clothesTag)
        val neededClothes = controller.population.toDouble() - clothesEvaluator.evaluatePack(controller.accessibleResources)
        if (neededClothes <= 0)
            return
        val types = setOf(Clothes, Comfort)
        val request = constructTagRequest(controller, clothesTag, neededClothes, types)
        _unfinishedRequestMap[request] = MutableResourcePack()
    }

    private fun addShelterRequest(controller: RequestConstructController) {
        val shelterEvaluator = tagEvaluator(shelterTag)
        val neededShelter = controller.population.toDouble() - shelterEvaluator.evaluatePack(controller.accessibleResources)
        if (neededShelter <= 0)
            return
        val types = setOf(Shelter, Comfort)
        val request = constructTagRequest(controller, shelterTag, neededShelter, types)
        _unfinishedRequestMap[request] = MutableResourcePack()
    }

    private fun constructFoodRequest(controller: RequestConstructController): Request {
        val foodFloor = (controller.population / controller.group.fertility + 1).toDouble()
        return TagRequest(
                foodTag,
                RequestCore(
                        controller.group,
                        foodFloor,
                        foodFloor + controller.population / 100 + 1,
                        if (controller.isClearEffects) passingReward else foodPenalty,
                        if (controller.isClearEffects) passingReward else foodReward,
                        99,
                        setOf(Food, Vital)
                )
        )
    }

    private fun constructWarmthRequest(controller: RequestConstructController) = TagRequest(
            warmthTag,
            RequestCore(
                    controller.group,
                    controller.population.toDouble(),
                    controller.population.toDouble(),
                    if (controller.isClearEffects) passingReward else warmthPenalty,
                    passingReward,
                    90,
                    setOf(Warmth, Vital)
            )
    )

    private fun constructTagRequest(
            controller: RequestConstructController,
            tag: ResourceTag,
            amount: Double,
            requestTypes: Set<RequestType>
    ): TagRequest {
        val temporaryCore = RequestCore(controller.group, amount, amount, put(), put(), 90, requestTypes)
        val temporaryRequest = TagRequest(tag, temporaryCore)
        val nerfed = getRequestNerfCoefficient(temporaryRequest, amount)
        return TagRequest(
                tag,
                RequestCore(
                        controller.group,
                        nerfed,
                        nerfed,
                        if (controller.isClearEffects) passingReward else unite(listOf(addNeed(TagLabeler(tag)), put())),
                        if (controller.isClearEffects) passingReward else put(),
                        90,
                        requestTypes
                )
        )
    }

    fun finishUpdate() {
        for ((req, res) in turnRequests.resultStatus) {
            nerfCoefficients[req] =
                    if (res.status == ResultStatus.NotSatisfied)
                        (nerfCoefficients[req] ?: 0) + 1
                    else
                        (nerfCoefficients[req] ?: 0) / 2
        }
    }

    override fun toString() =
            if (turnRequests.resultStatus.isNotEmpty())
                "Finished requests:\n" +
                        turnRequests.resultStatus.entries.joinToString("\n")
                        { (req, res) -> "$req - $res, unfinished estimation ${nerfCoefficients[req]}" }
            else "No requests were finished"
}

data class RequestConstructController(
        val group: Group,
        val population: Int,
        val accessibleResources: ResourcePack,
        val previous: RequestPool,
        val isClearEffects: Boolean = false
)
