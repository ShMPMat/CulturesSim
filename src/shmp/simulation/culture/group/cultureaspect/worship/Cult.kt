package shmp.simulation.culture.group.cultureaspect.worship

import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.chanceOf
import shmp.simulation.CulturesController.*
import shmp.simulation.culture.aspect.MadeByResourceFeature
import shmp.simulation.culture.aspect.MeaningResourceFeature
import shmp.simulation.culture.group.stratum.CultStratum
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.cultureaspect.worship.BuildingsType.*
import shmp.simulation.culture.group.cultureaspect.worship.CultType.*
import shmp.simulation.culture.group.cultureaspect.worship.OfferingType.*
import shmp.simulation.culture.group.passingReward
import shmp.simulation.culture.group.request.RequestCore
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.SimpleResourceRequest
import shmp.generator.culture.worldview.Meme
import shmp.simulation.space.resource.container.MutableResourcePack


class Cult(val name: String, type: CultType = Shaman, buildingsType: BuildingsType = NoBuildings) : BaseWorshipFeature() {
    var buildingsType = buildingsType
        private set

    var type = type
        private set

    internal fun findStratum(group: Group, parent: Worship) = group.populationCenter.stratumCenter
            .getByCultNameOrNull(parent.simpleName)
            ?: run {
                val newStratum = CultStratum(parent.simpleName, group.territoryCenter.center)
                group.populationCenter.stratumCenter.addStratum(newStratum)
                group.populationCenter.getStratumPeople(newStratum, 1)
                group.populationCenter.stratumCenter.getByCultNameOrNull(parent.simpleName)
                        ?: throw GroupError("Cannot create Stratum for $this")
            }

    override fun use(group: Group, parent: Worship) {
        super.use(group, parent)

        val stratum = findStratum(group, parent)

        when (type) {
            Shaman -> {
                isFunctioning = if (stratum.population < 1) {
                    group.populationCenter.getStratumPeople(stratum, 1)

                    stratum.population == 1
                } else true
            }
            is Institution -> {
                val institution = type as Institution

                0.01.chanceOf {
                    institution.peopleNeeded += 1
                }

                isFunctioning = if (stratum.population < institution.peopleNeeded) {
                    group.populationCenter.getStratumPeople(stratum, institution.peopleNeeded - stratum.population)

                    stratum.population >= institution.peopleNeeded
                } else true
            }
        }

        manageOfferings(group, parent)
        manageSpecialPlaces(group, parent)
    }

    private fun manageOfferings(group: Group, parent: Worship) {
        val basePractitionerOfferProb = when (type) {
            Shaman -> 0.01
            is Institution -> 0.5
        }
        val offeringsAmount = parent.features.filterIsInstance<Offering>().size

        (basePractitionerOfferProb / (offeringsAmount + 1)).chanceOf {
            makeWorshipObject(parent, group)?.let { resource ->
                parent.addFeature(Offering(
                        resource,
                        CultPractitioners,
                        RandomSingleton.random.nextInt(1, 30)
                ))
            }
        }

        if (buildingsType == One)
            (basePractitionerOfferProb / (offeringsAmount + 1)).chanceOf {
                makeWorshipObject(parent, group)?.let { resource ->
                    parent.addFeature(Offering(
                            resource,
                            LocalTemple,
                            RandomSingleton.random.nextInt(1, 30)
                    ))
                }
            }
    }

    private fun manageSpecialPlaces(group: Group, parent: Worship) {
        val makeTemple = when (type) {
            Shaman -> 0.01
            is Institution -> 0.1
        }

        makeTemple.chanceOf {
            buildingsType = One
        }

        if (buildingsType is One) {
            if (parent.placeSystem.places.isEmpty()) {
                parent.addWorshipPlace(group)

                if (parent.placeSystem.places.isEmpty())
                    return
            }

            val templeResource = session.world.resourcePool.getSimpleName("Temple")

            if (parent.placeSystem.places.first().staticPlace.getResource(templeResource).amount > 0)
                return

            val request = SimpleResourceRequest(
                    templeResource,
                    RequestCore(
                            group,
                            1.0,
                            1.0,
                            passingReward,
                            passingReward,
                            75,
                            setOf(RequestType.Spiritual)
                    )
            )
            val result = group.populationCenter.executeRequest(request)
            val pack = MutableResourcePack(result.pack)

            if (request.evaluator.evaluatePack(pack) != 0.0) {
                result.usedAspects.forEach { it.gainUsefulness(20) }
                val temple = request.evaluator.pickAndRemove(pack).resources
                        .map {
                            it.copyWithNewExternalFeatures(listOf(
                                    MeaningResourceFeature(Meme(name)),
                                    MadeByResourceFeature(group)
                            ))
                        }
                group.resourceCenter.addAll(temple)
                val place = parent.placeSystem.places
                        .minByOrNull { t -> t.staticPlace.owned.getAmount { it in temple } }
                        ?: throw GroupError("Couldn't find Special places")

                place.staticPlace.addResources(temple)
            }
        }
    }

    override fun adopt(group: Group) = Cult(name)

    override fun die(group: Group, parent: Worship) = Unit

    override fun swapWorship(worshipObject: WorshipObject) = Cult(name)

    override fun toString() = "$type"
}


sealed class CultType {
    object Shaman : CultType() {
        override fun toString() = "Shaman"
    }

    class Institution(var peopleNeeded: Int) : CultType() {
        override fun toString() = "Institution of size $peopleNeeded"
    }
}

sealed class BuildingsType {
    object NoBuildings : BuildingsType()

    object One : BuildingsType()
}
