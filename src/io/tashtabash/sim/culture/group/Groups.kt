package io.tashtabash.sim.culture.group

import io.tashtabash.sim.culture.foodTag
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.util.freeze
import io.tashtabash.sim.culture.group.centers.util.starve
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler

/**
 * This operation is not commutative.
 * @return double from 0 to 1.
 */
fun getGroupsDifference(g1: Group, g2: Group): Double {
    var matched = 1.0
    var overall = 1.0
    for (aspect in g1.cultureCenter.aspectCenter.aspectPool.all) {
        if (g2.cultureCenter.aspectCenter.aspectPool.contains(aspect)) {
            matched++
        }
        overall++
    }
    for (aspect in g1.cultureCenter.cultureAspectCenter.aspectPool.all) {
        if (g2.cultureCenter.cultureAspectCenter.aspectPool.contains(aspect)) {
            matched++
        }
        overall++
    }
    return matched / overall
}

val foodPenalty = { pair: Pair<Group, MutableResourcePack>, percent: Double ->
    starve(pair.first, percent)
    pair.second.destroyAllResourcesWithTag(foodTag)
}

val foodReward = { pair: Pair<Group, MutableResourcePack>, percent: Double ->
    pair.first.populationCenter.goodConditionsGrow(percent, pair.first.territoryCenter.territory)
    pair.second.destroyAllResourcesWithTag(foodTag)
}

val warmthPenalty = { pair: Pair<Group, MutableResourcePack>, percent: Double ->
    freeze(pair.first, percent)
}

val passingReward =  { _: Pair<Group, MutableResourcePack>, _: Double -> }

fun addNeed(labeler: ResourceLabeler) = { pair: Pair<Group, MutableResourcePack>, _: Double ->
    pair.first.cultureCenter.addAspiration(labeler)
}

fun unite(actions: List<(Pair<Group, MutableResourcePack>, Double) -> Unit>) = { pair: Pair<Group, MutableResourcePack>, percent: Double ->
    actions.forEach {
        it(pair, percent)
    }
}

fun put() = { pair: Pair<Group, MutableResourcePack>, _: Double ->
    pair.first.resourceCenter.addAll(pair.second)
}

//Aspects to add in newly created Groups.
val compulsoryAspects = listOf("TakeApart", "Take")
