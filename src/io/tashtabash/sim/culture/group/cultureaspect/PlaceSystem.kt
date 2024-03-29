package io.tashtabash.sim.culture.group.cultureaspect

import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.culture.group.GroupError
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.request.Request


class PlaceSystem(private val _places: MutableSet<SpecialPlace>, val hasOutsidePlaces: Boolean) : CultureAspect {
    val places: Set<SpecialPlace> = _places
    override fun getRequest(group: Group): Request? = null

    fun addPlace(placeSystem: SpecialPlace) {
        _places.add(placeSystem)
    }

    override fun use(group: Group) {
        places.forEach { it.use(group) }

        if (hasOutsidePlaces)
            return

        session.placeSystemLimitsCheck.chanceOf {
            checkLimits(group)
        }
    }

    private fun checkLimits(group: Group) = places
            .filter { !group.territoryCenter.territory.contains(it.staticPlace.tile) }
            .forEach { removePlace(it, group) }

    fun removePlace(place: SpecialPlace, group: Group) {
        if (_places.remove(place)) {
            place.die(group)
        } else throw GroupError("Trying to delete Place which is not owned")
    }

    override fun adopt(group: Group) = PlaceSystem(
            places.filter { group.territoryCenter.territory.contains(it.staticPlace.tile) }
                    .map { it.adopt(group) }
                    .toMutableSet(),
            hasOutsidePlaces
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
