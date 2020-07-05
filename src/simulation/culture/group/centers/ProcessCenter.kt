package simulation.culture.group.centers

import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.group.place.StaticPlace
import simulation.culture.group.process.behaviour.*
import simulation.culture.group.stratum.TraderStratum

class ProcessCenter(type: AdministrationType) {
    var type = type
        set(value) {
            field = value
        }

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
                .mapNotNull { it.update(group) }
                .toMutableList()

        return when (type) {
            AdministrationType.Main -> AddAdministrativeBehaviours(group)
            else -> {

            }
        }
    }

    fun update(group: Group) {
        if (testProbability(session.behaviourUpdateProb, session.random))
            updateBehaviours(group)

        runBehaviours(group)
    }

    private fun AddAdministrativeBehaviours(group: Group) {
        if (group.territoryCenter.settled) {
            if (behaviours.none { it is ManageRoadsBehaviour })
                behaviours.add(ManageRoadsBehaviour())
        }
    }


    private fun runBehaviours(group: Group) {
        val events = behaviours.flatMap {
            it.run(group)
        }
        events.forEach { group.addEvent(it) }
    }

    override fun toString() = "Type: $type\n" +
            "Behaviours:\n" +
            behaviours.joinToString("\n")
}

enum class AdministrationType {
    Subordinate,
    Main
}
