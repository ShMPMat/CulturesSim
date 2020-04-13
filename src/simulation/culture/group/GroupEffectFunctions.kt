package simulation.culture.group

import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.TagLabeler

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