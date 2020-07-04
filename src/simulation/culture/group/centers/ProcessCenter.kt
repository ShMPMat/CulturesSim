package simulation.culture.group.centers

import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.group.place.StaticPlace
import simulation.culture.group.process.behaviour.*
import simulation.culture.group.stratum.TraderStratum

class ProcessCenter(val type: AdministrationType) {
    private var behaviours: MutableList<GroupBehaviour> = mutableListOf(
            RandomArtifactBehaviour.withProbability(0.1),
            RandomTradeBehaviour.times(1, 5),
            RandomGroupAddBehaviour.withProbability(0.01),
            MakeTradeResourceBehaviour(5).times(
                    0,
                    1,
                    minUpdate = { g -> g.populationCenter.strata.first { it is TraderStratum }.population / 5 }
            )
    )

    private fun updateBehaviours(group: Group) {
        behaviours = behaviours
                .map { it.update(group) }
                .toMutableList()
    }

    fun update(group: Group) {
        if (testProbability(session.behaviourUpdateProb, session.random))
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

    override fun toString() = "Behaviours:\n" +
            behaviours.joinToString("\n")
}

enum class AdministrationType {
    Subordinate,
    Main
}
