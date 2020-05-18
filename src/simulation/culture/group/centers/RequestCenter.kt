package simulation.culture.group.centers

import simulation.culture.group.*
import simulation.culture.group.request.*
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.ResourcePack
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.TagLabeler
import java.util.*
import kotlin.math.max

class RequestCenter {
    private var _unfinishedRequestMap = mutableMapOf<Request, MutableResourcePack>()

    var turnRequests = RequestPool(HashMap())
        private set

    private val nerfCoefficients = mutableMapOf<Request, Int>()

    private fun getRequestNerfCoefficient(request: Request, amount: Int): Int {
        val coefficient = max(
                nerfCoefficients[request] ?: 1,
                1
        ).toDouble()
        return max(1.0, amount / coefficient).toInt()
    }

    fun updateRequests(group: Group) {
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

    private fun addWarmthRequest(controller: RequestConstructController) {
        if (controller.group.territoryCenter.territory.minTemperature < 0)
            _unfinishedRequestMap[constructWarmthRequest(controller)] = MutableResourcePack()
    }

    private fun addClothesRequest(controller: RequestConstructController) {
        val clothesEvaluator = tagEvaluator(ResourceTag("clothes"))
        val neededClothes = controller.population - clothesEvaluator.evaluate(controller.accessibleResources).toInt()
        if (neededClothes > 0) {
            _unfinishedRequestMap[constructTagRequest(controller, ResourceTag("clothes"), neededClothes)] =
                    MutableResourcePack()
        }
    }

    private fun addShelterRequest(controller: RequestConstructController) {
        val shelterEvaluator = tagEvaluator(ResourceTag("shelter"))
        val neededShelter = controller.population - shelterEvaluator.evaluate(controller.accessibleResources).toInt()
        if (neededShelter > 0) {
            _unfinishedRequestMap[constructTagRequest(controller, ResourceTag("shelter"), neededShelter)] =
                    MutableResourcePack()
        }
    }

    private fun constructFoodRequest(controller: RequestConstructController): Request {
        val foodFloor = controller.population / controller.group.fertility + 1
        return TagRequest(
                controller.group,
                ResourceTag("food"),
                foodFloor,
                foodFloor + controller.population / 100 + 1,
                if (controller.isClearEffects) passingReward else foodPenalty,
                if (controller.isClearEffects) passingReward else foodReward
        )
    }

    private fun constructWarmthRequest(controller: RequestConstructController) = TagRequest(
            controller.group,
            ResourceTag("warmth"),
            controller.population,
            controller.population,
            if (controller.isClearEffects) passingReward else warmthPenalty,
            passingReward
    )

    private fun constructTagRequest(controller: RequestConstructController, tag: ResourceTag, amount: Int): TagRequest {
        val notFinal = TagRequest(controller.group, tag, amount, amount, put(), put())
        val nerfed = getRequestNerfCoefficient(notFinal, amount)
        return TagRequest(
                controller.group,
                tag,
                nerfed,
                nerfed,
                if (controller.isClearEffects) passingReward else unite(listOf(addNeed(TagLabeler(tag)), put())),
                if (controller.isClearEffects) passingReward else put()
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

    override fun toString() = if (turnRequests.resultStatus.isEmpty()) "No requests were finished"
    else "Finished requests:\n" +
            turnRequests.resultStatus.entries.joinToString("\n")
            { (req, res) -> "$req - $res, unfinished estimation ${nerfCoefficients[req]}" }
}

data class RequestConstructController(
        val group: Group,
        val population: Int,
        val accessibleResources: ResourcePack,
        val previous: RequestPool,
        val isClearEffects: Boolean = false
)