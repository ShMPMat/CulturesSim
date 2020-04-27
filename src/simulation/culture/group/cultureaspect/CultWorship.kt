package simulation.culture.group.cultureaspect

import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.group.stratum.CultStratum
import simulation.culture.group.GroupError
import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request
import simulation.culture.group.request.ResourceRequest
import simulation.culture.group.request.SimpleResourceRequest
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.tag.labeler.SimpleNameLabeler

data class CultWorship(
        val worship: Worship
) : CultureAspect {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        worship.use(group)
        val stratum = group.populationCenter.strata
                .filterIsInstance<CultStratum>()
                .firstOrNull { it.cultName == toString() }
                ?: kotlin.run {
                    val newStratum = CultStratum(toString())
                    group.populationCenter.addStratum(newStratum)
                    group.populationCenter.changeStratumAmount(newStratum, 1)
                    group.populationCenter.strata
                            .filterIsInstance<CultStratum>()
                            .firstOrNull { it.cultName == toString() }
                            ?: throw GroupError("Cannot create Stratum for $this")
                }
        if (testProbability(0.01, session.random))
            group.populationCenter.changeStratumAmount(stratum, stratum.population + 1)
        manageSpecialPlaces(group)
    }

    private fun manageSpecialPlaces(group: Group) {
        if (worship.placeSystem.places.isEmpty()) return
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
            val pack = MutableResourcePack(group.populationCenter.executeRequest(request, group))
            if (request.evaluator.evaluate(pack) == 0) {
                group.resourceCenter.addNeeded(SimpleNameLabeler("Temple"), 100)
            } else {
                val temple = request.evaluator.pickAndRemove(pack)
                group.resourceCenter.addAll(pack)
                val place = worship.placeSystem.places
                        .minBy { t -> t.place.owned.getResources { it in temple.resources }.amount }
                if (place == null) {
                    throw GroupError("Couldn't find Special places")
                } else {
                    if (temple.resources.any { !it.fullName.contains("Root|Tree".toRegex()) }) {
                        val f = 0
                    }
                    place.place.addResources(temple)
                }
            }
        }
    }

    override fun copy(group: Group) = CultWorship(worship.copy(group))

    override fun toString(): String {
        return "Cult of $worship"
    }


}