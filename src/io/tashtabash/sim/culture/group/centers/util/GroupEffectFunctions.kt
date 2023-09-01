package io.tashtabash.sim.culture.group.centers.util

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.labeler.TagLabeler


fun starve(group: Group, fraction: Double) {
    val populationDecrease = (group.populationCenter.population / 10 * (1 - fraction) + 1).toInt()
    group.populationCenter.decreasePopulation(populationDecrease, "Not enough food $fraction")
    group.cultureCenter.addAspiration(TagLabeler(ResourceTag("food")))
}

fun freeze(group: Group, fraction: Double) {
    val populationDecrease = (group.populationCenter.population / 10 * (1 - fraction) + 1).toInt()
    group.populationCenter.decreasePopulation(populationDecrease, "Not enough warmth $fraction")
    group.cultureCenter.addAspiration(TagLabeler(ResourceTag("warmth")))
}
