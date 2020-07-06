package simulation.culture.group.centers

import shmp.random.testProbability
import simulation.Controller
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
            RandomTradeBehaviour.times(1, 3),
            RandomGroupAddBehaviour.withProbability(0.01),
            MakeTradeResourceBehaviour(5).times(
                    0,
                    1,
                    minUpdate = { g -> g.populationCenter.stratumCenter.traderStratum.population / 30 }
            )
    )

    private fun updateBehaviours(group: Group) {
        behaviours = behaviours
                .mapNotNull { it.update(group) }
                .toMutableList()

        when (type) {
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
        val main = System.nanoTime()
        val events = behaviours.flatMap {
            it.run(group)
        }
        events.forEach { group.addEvent(it) }
        Controller.session.groupMigrationTime += System.nanoTime() - main
    }

    override fun toString() = "Type: $type\n" +
            "Behaviours:\n" +
            behaviours.joinToString("\n")
}

enum class AdministrationType {
    Subordinate,
    Main
}
