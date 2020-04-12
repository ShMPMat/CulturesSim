package simulation.culture.group

import simulation.Controller
import simulation.space.resource.tag.ResourceTag

fun starve(group: Group, fraction: Double) {
    val populationDecrease = (group.populationCenter.population / 10 * (1 - fraction) + 1).toInt()
    group.populationCenter.decreasePopulation(populationDecrease)
    group.cultureCenter.addAspiration(Aspiration(10, ResourceTag("food"), Controller.session.world.turn))
}

fun freeze(group: Group, fraction: Double) {
    val populationDecrease = (group.populationCenter.population / 10 * (1 - fraction) + 1).toInt()
    group.populationCenter.decreasePopulation(populationDecrease)
    group.cultureCenter.addAspiration(Aspiration(10, ResourceTag("warmth"), Controller.session.world.turn))
}