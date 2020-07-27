package simulation.culture.group.centers

import shmp.random.testProbability
import simulation.Controller
import simulation.Controller.*
import simulation.culture.group.process.behaviour.*
import kotlin.math.pow

class ProcessCenter(type: AdministrationType) {
    var type = type
        set(value) {
            field = value
        }

    private var behaviours: MutableList<GroupBehaviour> = mutableListOf(
            RandomArtifactB.withProbability(0.1),
            RandomTradeB.times(1, 3),
            RandomGroupSeizureB.withProbability(0.01),
            MakeTradeResourceB(5).times(
                    0,
                    1,
                    minUpdate = { g -> g.populationCenter.stratumCenter.traderStratum.population / 30 }
            ),
            TurnRequestsHelpB(),
            SplitGroupB().withProbability(session.defaultGroupDiverge) {
                session.defaultGroupDiverge / (it.parentGroup.subgroups.size + 1)
            },
            TryDivergeB().withProbability(session.defaultGroupExiting) {
                it.populationCenter.maxPopulationPart(it.territoryCenter.territory) *
                        session.defaultGroupExiting /
                        it.relationCenter.getAvgConglomerateRelation(it.parentGroup).pow(2)
            },
            ManageOwnType().withProbability(session.defaultTypeRenewal) {
                session.defaultTypeRenewal / it.parentGroup.subgroups.size
            }
    )

    private val addedBehaviours = mutableListOf<GroupBehaviour>()

    fun addBehaviour(behaviour: AbstractGroupBehaviour) = addedBehaviours.add(behaviour)

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
        }.toMutableList()
        while (addedBehaviours.isNotEmpty()) {
            val newBehaviours = addedBehaviours.toList()
            addedBehaviours.clear()

            events += newBehaviours.flatMap {
                it.run(group)
            }
            behaviours.addAll(newBehaviours)
        }
        events.forEach { group.addEvent(it) }
    }

    override fun toString() = """
        |Type: $type
        |Behaviours:
        |${behaviours.joinToString("\n")}
        """.trimMargin()
}


enum class AdministrationType {
    Subordinate,
    Main
}

fun AdministrationType.getSubordinates(group: Group) = when (this) {
    AdministrationType.Main -> group.parentGroup.subgroups.filter { it != group }
    AdministrationType.Subordinate -> listOf()
}
