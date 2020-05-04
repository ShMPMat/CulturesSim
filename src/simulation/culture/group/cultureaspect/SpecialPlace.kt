package simulation.culture.group.cultureaspect

import shmp.random.testProbability
import simulation.Controller
import simulation.Controller.session
import simulation.culture.group.Place
import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request
import simulation.culture.group.request.resourceToRequest

class SpecialPlace(
        val place: Place
) : CultureAspect {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        if (testProbability(0.1, Controller.session.random)) {//TODO lesser amount
            val lacking = place.getLacking()
            val gotResources = lacking
                    .map { resourceToRequest(it, group, it.amount) }
                    .flatMap { group.populationCenter.executeRequest(it).pack.resources }
            place.addResources(gotResources)
        }
    }

    override fun copy(group: Group): SpecialPlace {
        return SpecialPlace(place)
    }

    override fun die(group: Group) {
        session.world.strayPlacesManager.addPlace(place)
    }

    override fun toString(): String {
        return "Special place on tile ${place.tile.x} ${place.tile.y}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpecialPlace

        if (place != other.place) return false

        return true
    }

    override fun hashCode(): Int {
        return place.hashCode()
    }
}