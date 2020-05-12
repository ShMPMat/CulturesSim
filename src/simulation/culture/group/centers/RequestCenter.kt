package simulation.culture.group.centers

import simulation.culture.group.*
import simulation.culture.group.request.*
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.TagLabeler
import java.util.*
import kotlin.math.max
import kotlin.math.sqrt

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
        _unfinishedRequestMap = mutableMapOf()

        addFoodRequest(group)

        addWarmthRequest(group)

        if (turnRequests.resultStatus.containsKey(constructFoodRequest(group)) &&
                turnRequests.resultStatus[constructFoodRequest(group)]?.status !== ResultStatus.NotSatisfied) {
            addClothesRequest(group)
            addShelterRequest(group)
        }

        group.cultureCenter.cultureAspectCenter.aspectPool.getAspectRequests(group)
                .filter { Objects.nonNull(it) }
                .forEach { _unfinishedRequestMap[it] = MutableResourcePack() }

        turnRequests = RequestPool(_unfinishedRequestMap)
    }

    private fun addFoodRequest(group: Group) {
        _unfinishedRequestMap[constructFoodRequest(group)] = MutableResourcePack()
    }

    private fun addWarmthRequest(group: Group) {
        if (group.territoryCenter.territory.minTemperature < 0)
            _unfinishedRequestMap[constructWarmthRequest(group)] = MutableResourcePack()
    }

    private fun addClothesRequest(group: Group) {
        val clothesEvaluator = tagEvaluator(ResourceTag("clothes"))
        val neededClothes = group.populationCenter.population -
                clothesEvaluator.evaluate(group.resourceCenter.pack).toInt()
        if (neededClothes > 0) {
            _unfinishedRequestMap[constructTagRequest(group, ResourceTag("clothes"), neededClothes)] =
                    MutableResourcePack()
        }
    }

    private fun addShelterRequest(group: Group) {
        val shelterEvaluator = tagEvaluator(ResourceTag("shelter"))
        val neededShelter = group.populationCenter.population -
                shelterEvaluator.evaluate(group.resourceCenter.pack).toInt()
        if (neededShelter > 0) {
            _unfinishedRequestMap[constructTagRequest(group, ResourceTag("shelter"), neededShelter)] =
                    MutableResourcePack()
        }
    }

    private fun constructFoodRequest(group: Group): Request {
        val foodFloor = group.populationCenter.population / group.fertility + 1
        return TagRequest(
                group,
                ResourceTag("food"),
                foodFloor,
                foodFloor + group.populationCenter.population / 100 + 1,
                foodPenalty,
                foodReward
        )
    }

    private fun constructWarmthRequest(group: Group) = TagRequest(
            group,
            ResourceTag("warmth"),
            group.populationCenter.population,
            group.populationCenter.population,
            warmthPenalty,
            passingReward
    )

    private fun constructTagRequest(group: Group, tag: ResourceTag, amount: Int): TagRequest {
        val notFinal = TagRequest(group, tag, amount, amount, put(), put())
        val nerfed = getRequestNerfCoefficient(notFinal, amount)
        return TagRequest(
                group,
                tag,
                nerfed,
                nerfed,
                unite(listOf(addNeed(TagLabeler(tag)), put())),
                put()
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