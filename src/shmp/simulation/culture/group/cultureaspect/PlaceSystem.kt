package shmp.simulation.culture.group.cultureaspect

import shmp.random.testProbability
import shmp.simulation.Controller.*
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.request.Request

class PlaceSystem(
        val places: MutableSet<SpecialPlace>
) : CultureAspect {
    override fun getRequest(group: Group): Request? = null

    fun addPlace(placeSystem: SpecialPlace) {
        places.add(placeSystem)
    }

    override fun use(group: Group) {
        places.forEach { it.use(group) }
        if (testProbability(session.placeSystemLimitsCheck, session.random))
            checkLimits(group)
    }

    private fun checkLimits(group: Group) {
        places.filter { !group.territoryCenter.territory.contains(it.staticPlace.tile) }
                .forEach { removePlace(it, group) }
    }

    fun removePlace(place: SpecialPlace, group: Group) {
        if (places.remove(place)) {
            place.die(group)
        } else throw GroupError("Trying to delete Place which is not owned")
    }

    override fun adopt(group: Group) = PlaceSystem(
            places.filter { group.territoryCenter.territory.contains(it.staticPlace.tile) }
                    .map { it.adopt(group) }
                    .toMutableSet()
    )

    override fun die(group: Group) = places.forEach { it.die(group) }

    override fun toString() = "Places: " + places.joinToString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceSystem

        if (places != other.places) return false

        return true
    }

    override fun hashCode() = places.hashCode()
}