package simulation.culture.group.cultureaspect

import simulation.culture.group.Place
import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request

class SpecialPlace(
        val place: Place
) : CultureAspect {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {}

    override fun copy(group: Group): SpecialPlace {
        return SpecialPlace(place)
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