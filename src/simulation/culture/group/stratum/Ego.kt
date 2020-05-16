package simulation.culture.group.stratum

import shmp.random.testProbability
import simulation.Controller.session
import simulation.culture.group.centers.Group
import simulation.culture.group.place.MovablePlace
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack
import simulation.space.tile.Tile
import simulation.space.tile.TileTag

class Ego(tile: Tile, name: String) {
    var isActive = false
    val place = MovablePlace(tile, TileTag(name, "stratum"))

    fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group
    ) {
        if (!isActive) return
        if (!testProbability(session.egoRenewalProb, session.random)) return
        val best = accessibleResources.resources
                .map {group.cultureCenter.evaluateResource(it) to it }
                .filter { it.first > 2 }
                .sortedBy { it.first }
                .take(5)
        best.forEach { group.cultureCenter.evaluateResource(it.second) }//TODO Place!
    }
}