package simulation.culture.group.stratum

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller.session
import simulation.culture.group.centers.Group
import simulation.culture.group.place.MovablePlace
import simulation.culture.group.request.resourceToRequest
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
        val best = accessibleResources.resources.asSequence()
                .map { group.cultureCenter.evaluateResource(it) to it }
                .filter { it.first > 2 }
                .sortedBy { it.first }
                .take(1).toList()
        place.current.addResources(best.map { it.second })
        val allGoodProduced = group.cultureCenter.aspectCenter.aspectPool.producedResources.asSequence()
                .map { group.cultureCenter.evaluateResource(it.first).toDouble() to it.first }
                .filter { it.first > 2 }
                .sortedBy { it.first }.toList()
        if (allGoodProduced.isNotEmpty()) {
            val chosen = randomElement(allGoodProduced, { it.first }, session.random)
            val request = resourceToRequest(chosen.second, group, 1)
            val result = group.populationCenter.executeRequest(request).pack
            place.current.addResources(result)
            allGoodProduced.forEach { group.cultureCenter.evaluateResource(it.second) }
        }
    }

}