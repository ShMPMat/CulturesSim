package simulation.culture.group.cultureaspect

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
    }

    override fun copy(group: Group): PlaceSystem {
        return PlaceSystem(places.map { it.copy(group) }.toMutableSet())
    }

    override fun toString(): String {
        return "Places: " + places.joinToString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceSystem

        if (places != other.places) return false

        return true
    }

    override fun hashCode(): Int {
        return places.hashCode()
    }
}