package simulation.culture.group.cultureaspect

import shmp.random.testProbability
import simulation.Controller
import simulation.Controller.session
import simulation.culture.group.place.StaticPlace
import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request
import simulation.culture.group.request.resourceToRequest

class SpecialPlace(
        val staticPlace: StaticPlace
) : CultureAspect {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        if (testProbability(0.1, session.random)) {//TODO lesser amount
            val lacking = staticPlace.getLacking()
            val gotResources = lacking
                    .map { resourceToRequest(it, group, it.amount) }
                    .flatMap { group.populationCenter.executeRequest(it).pack.resources }
            staticPlace.addResources(gotResources)
        }
    }

    override fun adopt(group: Group) = SpecialPlace(staticPlace)

    override fun die(group: Group) {
        session.world.strayPlacesManager.addPlace(staticPlace)
    }

    override fun toString(): String {
        return "Special place on tile ${staticPlace.tile.x} ${staticPlace.tile.y}"
    }

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