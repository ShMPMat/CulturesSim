package simulation.culture.group.cultureaspect.worship

import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.group.stratum.CultStratum
import simulation.culture.group.GroupError
import simulation.culture.group.centers.Group
import simulation.culture.group.request.SimpleResourceRequest
import simulation.culture.group.stratum.Stratum
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.tag.labeler.SimpleNameLabeler

class Cult : WorshipFeature {
    override fun use(group: Group, parent: Worship) {
        val stratum: Stratum = group.populationCenter.strata
                .filterIsInstance<CultStratum>()
                .firstOrNull { it.cultName == parent.simpleName }
                ?: kotlin.run {
                    val newStratum = CultStratum(parent.simpleName)
                    group.populationCenter.addStratum(newStratum)
                    group.populationCenter.changeStratumAmount(newStratum, 1)
                    group.populationCenter.strata
                            .filterIsInstance<CultStratum>()
                            .firstOrNull { it.cultName == parent.simpleName }
                            ?: throw GroupError("Cannot create Stratum for $this")
                }
        if (testProbability(0.01, session.random))
            group.populationCenter.changeStratumAmount(stratum, stratum.population + 1)
        manageSpecialPlaces(group, parent)
    }

    private fun manageSpecialPlaces(group: Group, parent: Worship) {
        if (parent.placeSystem.places.isEmpty()) return
        if (testProbability(0.1, session.random)) {//TODO lesser probability
            val templeResource = session.world.resourcePool.get("Temple")
            val request = SimpleResourceRequest(
                    group,
                    templeResource,
                    1,
                    1,
                    { _, _ -> },
                    { _, _ -> }
            )
            val result = group.populationCenter.executeRequest(request)
            val pack = MutableResourcePack(result.pack)
            if (request.evaluator.evaluate(pack) == 0.0) {
                group.resourceCenter.addNeeded(SimpleNameLabeler("Temple"), 100)
            } else {
                result.usedAspects.forEach { it.gainUsefulness(20) }
                val temple = request.evaluator.pickAndRemove(pack)
                group.resourceCenter.addAll(pack)
                val place = parent.placeSystem.places
                        .minBy { t -> t.staticPlace.owned.getResources { it in temple.resources }.amount }
                if (place == null) {
                    throw GroupError("Couldn't find Special places")
                } else {
                    place.staticPlace.addResources(temple)
                }
            }
        }
    }

    override fun adopt(group: Group) = Cult()

    override fun die(group: Group, parent: Worship) = Unit

    override fun swapWorship(worshipObject: WorshipObject) = Cult()

    override fun toString() = "Cult"
}