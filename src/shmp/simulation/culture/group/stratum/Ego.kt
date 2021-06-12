package shmp.simulation.culture.group.stratum

import shmp.random.*
import shmp.random.singleton.chanceOf
import shmp.simulation.CulturesController.session
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.RequestConstructController
import shmp.simulation.culture.group.place.MovablePlace
import shmp.simulation.culture.group.request.RequestPool
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.resourceToRequest
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.tile.Tile
import shmp.simulation.space.tile.TileTag
import java.util.HashMap

class Ego(tile: Tile, name: String) {
    var isActive = false
    val place = MovablePlace(tile, TileTag(name, "Stratum people"))
    var turnRequests = RequestPool(HashMap())
        private set

    fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group,
            parent: Stratum
    ) {
        if (!isActive)
            return
        session.egoRenewalProb.chanceOf {
            return
        }

        takeGoodResources(accessibleResources, group)
        manageRequests(group, parent)
    }

    private fun takeGoodResources(accessibleResources: MutableResourcePack, group: Group) {
        val best = accessibleResources.resources.asSequence()
                .map { group.cultureCenter.evaluateResource(it) to it }
                .filter { it.first > 2 }
                .sortedBy { it.first }
                .take(1).toList()
        place.current.addResources(best.map { it.second })
        val allGoodProduced = group.cultureCenter.aspectCenter.aspectPool.producedResources
                .asSequence()
                .map { it.toSampleSpaceObject(group.cultureCenter.evaluateResource(it).toDouble()) }
                .filter { it.probability > 3.0 }
                .toList()

        randomUnwrappedElementOrNull(allGoodProduced, session.random)?.let { chosen ->
            val request = resourceToRequest(chosen, group, 1, 50, setOf(RequestType.Luxury))
            val result = group.populationCenter.executeRequest(request).pack
            place.current.addResources(result)
        }
    }

    private fun manageRequests(group: Group, parent: Stratum) {
        turnRequests = group.cultureCenter.requestCenter.constructRequests(RequestConstructController(
                group,
                parent.population,
                place.current.owned,
                turnRequests,
                true
        ))
        group.populationCenter.executeRequests(turnRequests)
        turnRequests.finish()
    }
}
