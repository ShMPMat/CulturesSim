package simulation.culture.group.cultureaspect

import shmp.random.testProbability
import simulation.Controller
import simulation.Controller.*
import simulation.culture.group.GroupError
import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request
import simulation.culture.thinking.meaning.Meme
import java.util.*

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
        places.filter { !group.territoryCenter.territory.contains(it.place.tile) }
                .forEach { removePlace(it) }
    }

    fun removePlace(place: SpecialPlace) {
        if (places.remove(place)) {
            place.place.delete()
        } else throw GroupError("Trying to delete Place which is not owned")
    }

    override fun copy(group: Group) = PlaceSystem(
            places.filter { group.territoryCenter.territory.contains(it.place.tile) }
                    .map { it.copy(group) }
                    .toMutableSet()
    )

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