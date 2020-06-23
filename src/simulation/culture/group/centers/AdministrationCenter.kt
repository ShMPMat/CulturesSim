package simulation.culture.group.centers

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.cultureaspect.SpecialPlace
import simulation.space.resource.Resource

class AdministrationCenter(val type: AdministrationType) {
    private val infrastructure = mutableListOf<Resource>()

    fun update(group: Group) {
        return when (type) {
            AdministrationType.Main -> administrate(group)
            else -> {
            }
        }
    }

    private fun administrate(group: Group) {
        if (group.territoryCenter.settled) {
            if (testProbability(0.9, Controller.session.random))
                return
            val places = getAllPlaceLocations(group)
        }
    }

    private fun getAllPlaceLocations(group: Group): List<SpecialPlace> {
        var places = mutableListOf<SpecialPlace>()
        for (subgroup in group.parentGroup.subgroups) {
            places.addAll(
                    group.populationCenter.strata.flatMap { it.places }
            )
            places.addAll(
                    group.cultureCenter.cultureAspectCenter.aspectPool.worships.flatMap { it.placeSystem.places }
            )
        }
        places = places
                .filter { it.staticPlace.owned.isNotEmpty }
                .toMutableList()
        places.addAll(group.parentGroup.subgroups.flatMap { it.territoryCenter.places })
        places = places
                .distinctBy { it.staticPlace.tile }
                .toMutableList()
        if (places.size > 1) {
            val k = 0
        }
        return places
    }
}

enum class AdministrationType {
    Subordinate,
    Main
}