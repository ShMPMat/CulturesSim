package simulation.culture.group

import shmp.random.testProbability
import simulation.Controller.*

class StrayPlacesManager() {
    private val places = mutableSetOf<Place>()

    fun addPlace(place: Place) = places.add(place)

    fun update() {
        if (!testProbability(session.strayPlacesUpdate, session.random))
            return
        val deleted = mutableListOf<Place>()
        places.forEach {
            if (it.owned.isEmpty) {
                it.delete()
                deleted.add(it)
            }
        }
        places.removeAll(deleted)
    }
}