package simulation.culture.group.cultureaspect.worship

import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.aspect.MadeByResourceFeature
import simulation.culture.aspect.MeaningResourceFeature
import simulation.culture.group.stratum.CultStratum
import simulation.culture.group.GroupError
import simulation.culture.group.centers.Group
import simulation.culture.group.passingReward
import simulation.culture.group.request.RequestCore
import simulation.culture.group.request.SimpleResourceRequest
import simulation.culture.group.stratum.Stratum
import simulation.culture.thinking.meaning.MemeSubject
import simulation.space.resource.container.MutableResourcePack

class Cult(val name: String) : WorshipFeature {
    override fun use(group: Group, parent: Worship) {
        val stratum: Stratum = group.populationCenter.stratumCenter.getByCultNameOrNull(parent.simpleName)
                ?: kotlin.run {
                    val newStratum = CultStratum(parent.simpleName, group.territoryCenter.center)
                    group.populationCenter.stratumCenter.addStratum(newStratum)
                    group.populationCenter.getStratumPeople(newStratum, 1)
                    group.populationCenter.stratumCenter.getByCultNameOrNull(parent.simpleName)
                            ?: throw GroupError("Cannot create Stratum for $this")
                }
        if (testProbability(0.01, session.random))
            group.populationCenter.getStratumPeople(stratum, stratum.population + 1)

        manageSpecialPlaces(group, parent)
    }

    private fun manageSpecialPlaces(group: Group, parent: Worship) {
        if (parent.placeSystem.places.isEmpty()) return
        if (testProbability(0.1, session.random)) {//TODO lesser probability
            val request = SimpleResourceRequest(
                    session.world.resourcePool.getSimpleName("Temple"),
                    RequestCore(
                            group,
                            1.0,
                            1.0,
                            passingReward,
                            passingReward,
                            75
                    )
            )
            val result = group.populationCenter.executeRequest(request)
            val pack = MutableResourcePack(result.pack)

            if (request.evaluator.evaluate(pack) != 0.0) {
                result.usedAspects.forEach { it.gainUsefulness(20) }
                val temple = request.evaluator.pickAndRemove(pack).resources
                        .map {
                            it.copyWithNewExternalFeatures(listOf(
                                    MeaningResourceFeature(MemeSubject(name)),
                                    MadeByResourceFeature(group)
                            ))
                        }
                group.resourceCenter.addAll(temple)
                val place = parent.placeSystem.places
                        .minBy { t -> t.staticPlace.owned.getResources { it in temple }.amount }

                if (place == null)
                    throw GroupError("Couldn't find Special places")
                else
                    place.staticPlace.addResources(temple)
            }
        }
    }

    override fun adopt(group: Group) = Cult(name)

    override fun die(group: Group, parent: Worship) = Unit

    override fun swapWorship(worshipObject: WorshipObject) = Cult(name)

    override fun toString() = "Cult"
}