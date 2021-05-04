package shmp.simulation.culture.group.cultureaspect

import shmp.random.testProbability
import shmp.simulation.CulturesController.session
import shmp.simulation.culture.group.place.StaticPlace
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.group.request.resourceToRequest

class SpecialPlace(
        val staticPlace: StaticPlace
) : CultureAspect {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        if (testProbability(0.1, session.random)) {
            val lacking = staticPlace.getLacking()
            val gotResources = lacking
                    .map { resourceToRequest(it, group, it.amount, 75,  setOf()) }
                    .flatMap { group.populationCenter.executeRequest(it).pack.resources }
            staticPlace.addResources(gotResources)
        }
    }

    override fun adopt(group: Group) = SpecialPlace(staticPlace)

    override fun die(group: Group) {
        session.world.strayPlacesManager.addPlace(staticPlace)
    }

    override fun toString() = "Special place on tile ${staticPlace.tile.posStr}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpecialPlace

        if (staticPlace != other.staticPlace) return false

        return true
    }

    override fun hashCode(): Int {
        return staticPlace.hashCode()
    }
}