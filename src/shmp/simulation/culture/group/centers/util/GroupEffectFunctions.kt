package shmp.simulation.culture.group.centers.util

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.labeler.TagLabeler


fun starve(group: Group, fraction: Double) {
    val populationDecrease = (group.populationCenter.population / 10 * (1 - fraction) + 1).toInt()
    group.populationCenter.decreasePopulation(populationDecrease)
    group.cultureCenter.addAspiration(TagLabeler(ResourceTag("food")))
}

fun freeze(group: Group, fraction: Double) {
    val populationDecrease = (group.populationCenter.population / 10 * (1 - fraction) + 1).toInt()
    group.populationCenter.decreasePopulation(populationDecrease)
    group.cultureCenter.addAspiration(TagLabeler(ResourceTag("warmth")))
}
