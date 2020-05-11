package simulation.culture.group.centers

import simulation.culture.group.*
import simulation.culture.group.request.*
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.TagLabeler
import java.util.*
import java.util.List

class RequestCenter {
    private var _unfinishedRequestMap = mutableMapOf<Request, MutableResourcePack>()
    var turnRequests = RequestPool(HashMap())
        private set

    fun updateRequests(group: Group) {
        _unfinishedRequestMap = mutableMapOf()

        addFoodRequest(group)

        addWarmthRequest(group)

        if (turnRequests.resultStatus.containsKey(constructFoodRequest(group)) &&
                turnRequests.resultStatus[constructFoodRequest(group)]?.status !== ResultStatus.NotSatisfied) {
            val clothesEvaluator = tagEvaluator(ResourceTag("clothes"))
            val neededClothes = group.populationCenter.population -
                    clothesEvaluator.evaluate(group.resourceCenter.pack).toInt()
            if (neededClothes > 0) {
                _unfinishedRequestMap[constructTagRequest(group, ResourceTag("clothes"), neededClothes)] =
                        MutableResourcePack()
            } else if (group.populationCenter.population > 0) {
                val h = 0
            }
            val shelterEvaluator = tagEvaluator(ResourceTag("shelter"))
            val neededShelter = group.populationCenter.population -
                    shelterEvaluator.evaluate(group.resourceCenter.pack).toInt()
            if (neededShelter > 0) {
                _unfinishedRequestMap[constructTagRequest(group, ResourceTag("shelter"), neededClothes)] =
                        MutableResourcePack()
            } else if (group.populationCenter.population > 0) {
                val h = 0
            }
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

    private fun constructTagRequest(group: Group, tag: ResourceTag, amount: Int) = TagRequest(
            group,
            tag,
            amount,
            amount,
            unite(listOf(addNeed(TagLabeler(tag)), put())),
            put()
    )
}