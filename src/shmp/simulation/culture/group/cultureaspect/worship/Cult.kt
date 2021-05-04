package shmp.simulation.culture.group.cultureaspect.worship

import shmp.random.singleton.chanceOf
import shmp.simulation.CulturesController.*
import shmp.simulation.culture.aspect.MadeByResourceFeature
import shmp.simulation.culture.aspect.MeaningResourceFeature
import shmp.simulation.culture.group.stratum.CultStratum
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.passingReward
import shmp.simulation.culture.group.request.RequestCore
import shmp.simulation.culture.group.request.SimpleResourceRequest
import shmp.simulation.culture.group.stratum.Stratum
import shmp.simulation.culture.thinking.meaning.MemeSubject
import shmp.simulation.space.resource.container.MutableResourcePack


class Cult(val name: String) : WorshipFeature {
    override fun use(group: Group, parent: Worship) {
        val stratum: Stratum = group.populationCenter.stratumCenter.getByCultNameOrNull(parent.simpleName)
                ?: run {
                    val newStratum = CultStratum(parent.simpleName, group.territoryCenter.center)
                    group.populationCenter.stratumCenter.addStratum(newStratum)
                    group.populationCenter.getStratumPeople(newStratum, 1)
                    group.populationCenter.stratumCenter.getByCultNameOrNull(parent.simpleName)
                            ?: throw GroupError("Cannot create Stratum for $this")
                }
        0.01.chanceOf {
            group.populationCenter.getStratumPeople(stratum, stratum.population + 1)
        }

        manageSpecialPlaces(group, parent)
    }

    private fun manageSpecialPlaces(group: Group, parent: Worship) {
        if (parent.placeSystem.places.isEmpty()) return
        0.1.chanceOf {//TODO lesser probability
            val request = SimpleResourceRequest(
                    session.world.resourcePool.getSimpleName("Temple"),
                    RequestCore(
                            group,
                            1.0,
                            1.0,
                            passingReward,
                            passingReward,
                            75,
                            setOf()
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
                        .minByOrNull { t -> t.staticPlace.owned.getResources { it in temple }.amount }
                        ?: throw GroupError("Couldn't find Special places")

                place.staticPlace.addResources(temple)
            }
        }
    }

    override fun adopt(group: Group) = Cult(name)

    override fun die(group: Group, parent: Worship) = Unit

    override fun swapWorship(worshipObject: WorshipObject) = Cult(name)

    override fun toString() = "Cult"
}