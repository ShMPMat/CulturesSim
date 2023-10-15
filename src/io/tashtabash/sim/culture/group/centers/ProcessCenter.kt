package io.tashtabash.sim.culture.group.centers

import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.culture.group.cultureaspect.DepictObject
import io.tashtabash.sim.culture.group.process.*
import io.tashtabash.sim.culture.group.process.behaviour.*
import io.tashtabash.sim.interactionmodel.CulturesMapModel
import kotlin.math.pow


class ProcessCenter(type: AdministrationType) {
    var type = type
        set(value) {
            field = value
        }

    private var _behaviours: MutableList<GroupBehaviour> = mutableListOf(
            RandomArtifactB.withTrait(Trait.Creation.get().pow(0.25)),
            RandomDepictCaB.withTrait(Trait.Creation.get() / 10).withProbability(1.0) {
                1.0 / (it.cultureCenter.cultureAspectCenter.aspectPool.all.filterIsInstance<DepictObject>().size + 1)
            },
            MutateCultureAspectsB.withProbability(session.groupCultureAspectCollapse),
            CreateCultureAspectsB.withTrait(Trait.Creation.get()),
            MutateAspectsB
                    .withTrait(Trait.Discovery.get() * 3)
                    .withProbability(1.0) {
                        1.0 / (it.cultureCenter.aspectCenter.aspectPool.all.size + 1)
                    },
            PerceiveSurroundingTerritoryB,

            RandomTradeB.times(1, 3),
            RandomGroupSeizureB.withTrait(Trait.Expansion.get() * 0.04),
            MakeTradeResourceB(5).times(
                    0,
                    1,
                    minUpdate = { g -> g.populationCenter.stratumCenter.traderStratum.population / 30 }
            ),
            TurnRequestsHelpB,
            GiveGiftB.withTrait(Trait.Peace.get() * 0.2),
            SplitGroupB.withProbability(session.defaultGroupDiverge) {
                session.defaultGroupDiverge / (it.parentGroup.subgroups.size + 1)
            },
            TryDivergeWithNegotiationB
                    .withTrait(Trait.Consolidation.getNegative() * 2)
                    .withProbability(session.defaultGroupExiting) {
                        it.populationCenter.maxPopulationPart(it.territoryCenter.territory) *
                                session.defaultGroupExiting /
                                it.relationCenter.getAvgConglomerateRelation(it.parentGroup).pow(2)
                    },
            ManageOwnType.withProbability(session.defaultTypeRenewal) {
                session.defaultTypeRenewal / it.parentGroup.subgroups.size
            },
            EstablishTradeRelationsB.withProbability(0.0) {
                it.populationCenter.stratumCenter.traderStratum.cumulativeWorkAblePopulation / 100.0
            },
            RandomWarB.withTrait(Trait.Peace.getNegative() * Trait.Expansion.getPositive()),
            InternalConsolidationB.withTrait(Trait.Consolidation.getPositive()),
            DefenceFromNatureB,
            ManageDefenceB,
            ResolveResourceNeedB
    )

    val behaviours: List<GroupBehaviour> = _behaviours

    private val addedBehaviours = mutableListOf<GroupBehaviour>()

    fun addBehaviour(behaviour: GroupBehaviour) =
            addedBehaviours.add(behaviour)

    private fun addBehaviours(behaviours: List<GroupBehaviour>) =
            behaviours.forEach { addBehaviour(it) }

    private fun updateBehaviours(group: Group) {
        _behaviours = _behaviours
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
        session.behaviourUpdateProb.chanceOf {
            updateBehaviours(group)
        }

        if (_behaviours.size > 20)
            updateBehaviours(group)

        runBehaviours(group)
        session.interactionModel.let {
            if (it is CulturesMapModel)
                it.groupMigrationTime += System.nanoTime() - main
        }
    }

    private fun AddAdministrativeBehaviours(group: Group) {
        if (group.territoryCenter.settled)
            if (_behaviours.none { it is ManageRoadsB })
                _behaviours.add(ManageRoadsB())
    }

    private fun runBehaviours(group: Group) {
        _behaviours.forEach {
            consumeProcessResult(group, it.run(group))
        }

        while (addedBehaviours.isNotEmpty()) {
            val newBehaviours = addedBehaviours.toList()
            addedBehaviours.clear()

            newBehaviours.forEach {
                consumeProcessResult(group, it.run(group))
            }

            _behaviours.addAll(newBehaviours)
        }
    }

    fun consumeProcessResult(group: Group, result: ProcessResult) {
        addBehaviours(result.behaviours)
        group.addEvents(result.events)
        group.cultureCenter.memePool.strengthenMemes(result.memes)
        group.cultureCenter.consumeAllTraitChanges(result.traitChanges)
    }

    override fun toString() = """
        |Type: $type
        |Behaviours:
        |${_behaviours.joinToString("\n")}
        """.trimMargin()
}


enum class AdministrationType {
    Subordinate,
    Main
}

fun AdministrationType.getSubordinates(group: Group) = when (this) {
    AdministrationType.Main -> group.parentGroup.subgroups
            .filter { it.processCenter.type == AdministrationType.Subordinate }
    AdministrationType.Subordinate -> listOf()
}
