package simulation.culture.group.centers

import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.cultureaspect.SpecialPlace
import simulation.culture.group.intergroup.GroupBehaviour
import simulation.culture.group.intergroup.RandomGroupAddBehaviour
import simulation.culture.group.intergroup.RandomTradeBehaviour
import simulation.culture.group.intergroup.withProbability
import simulation.space.resource.Resource

class AdministrationCenter(val type: AdministrationType) {
    private val infrastructure = mutableListOf<Resource>()
    private val behaviours: MutableList<GroupBehaviour> = mutableListOf(
            RandomTradeBehaviour,
            RandomGroupAddBehaviour.withProbability(0.01)
    )

    fun update(group: Group) {
        runBehaviours(group)
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

    private fun runBehaviours(group: Group) {
        val events = behaviours.mapNotNull {
            it.run(group)
        }
        events.forEach { group.addEvent(it) }
    }
}

enum class AdministrationType {
    Subordinate,
    Main
}
