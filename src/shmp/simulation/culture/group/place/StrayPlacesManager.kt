package shmp.simulation.culture.group.place

import shmp.simulation.CulturesController.Companion.session
import shmp.random.testProbability


class StrayPlacesManager {
    private val places = mutableSetOf<StaticPlace>()

    fun addPlace(staticPlace: StaticPlace) = places.add(staticPlace)

    fun update() {
        if (!testProbability(session.strayPlacesUpdate, session.random))
            return
        val deleted = mutableListOf<StaticPlace>()
        places.forEach {
            if (it.owned.isEmpty) {
                it.delete()
                deleted.add(it)
            }
        }
        places.removeAll(deleted)
    }
}
