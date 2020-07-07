package simulation.culture.group.centers

import shmp.random.testProbability
import simulation.Controller
import simulation.Controller.*
import simulation.culture.group.process.behaviour.*

class ProcessCenter(type: AdministrationType) {
    var type = type
        set(value) {
            field = value
        }

    private var behaviours: MutableList<GroupBehaviour> = mutableListOf(
            RandomArtifactB.withProbability(0.1),
            RandomTradeB.times(1, 3),
            RandomGroupAddB.withProbability(0.01),
            MakeTradeResourceB(5).times(
                    0,
                    1,
                    minUpdate = { g -> g.populationCenter.stratumCenter.traderStratum.population / 30 }
            ),
            TurnRequestsHelpB()
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
        val main = System.nanoTime()
        if (testProbability(session.behaviourUpdateProb, session.random))
            updateBehaviours(group)

        runBehaviours(group)
        Controller.session.groupMigrationTime += System.nanoTime() - main
    }

    private fun AddAdministrativeBehaviours(group: Group) {
        if (group.territoryCenter.settled)
            if (behaviours.none { it is ManageRoadsB })
                behaviours.add(ManageRoadsB())
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
