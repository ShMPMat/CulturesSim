package simulation.culture.group.centers

import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.group.cultureaspect.SpecialPlace
import simulation.culture.group.intergroup.*
import simulation.culture.group.place.StaticPlace
import simulation.culture.group.stratum.TraderStratum

class AdministrationCenter(val type: AdministrationType) {
    private var behaviours: MutableList<GroupBehaviour> = mutableListOf()


    private fun updateBehaviours(group: Group) {
        behaviours = mutableListOf(
                RandomTradeBehaviour.times(1, 5),
                RandomGroupAddBehaviour.withProbability(0.01)
        )
    }

    fun update(group: Group) {
        if (behaviours.isEmpty() || testProbability(session.behaviourUpdateProb, session.random))
            updateBehaviours(group)

        runBehaviours(group)
        return when (type) {
            AdministrationType.Main -> administrate(group)
            else -> {

            }
        }
    }

    private fun administrate(group: Group) {
        if (group.territoryCenter.settled) {
            if (testProbability(0.9, session.random))
                return
            val places = getAllPlaceLocations(group)
        }
    }

    private fun getAllPlaceLocations(group: Group): List<StaticPlace> {
        var places = mutableListOf<StaticPlace>()
        for (subgroup in group.parentGroup.subgroups) {
            places.addAll(
                    group.populationCenter.strata.flatMap { it.places }
            )
            places.addAll(
                    group.cultureCenter.cultureAspectCenter.aspectPool.worships
                            .flatMap { it.placeSystem.places }
                            .map { it.staticPlace }
            )
        }
        places = places
                .filter { it.owned.isNotEmpty }
                .toMutableList()
        places.addAll(group.parentGroup.subgroups.flatMap { it.territoryCenter.places })
        places = places
                .distinctBy { it.tile }
                .toMutableList()
        if (places.size > 1) {
            val k = 0
        }
        return places
    }

    private fun runBehaviours(group: Group) {
        val events = behaviours.flatMap {
            it.run(group)
        }
        events.forEach { group.addEvent(it) }
    }
}

enum class AdministrationType {
    Subordinate,
    Main
}
