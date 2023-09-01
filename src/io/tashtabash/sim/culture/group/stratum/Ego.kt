package io.tashtabash.sim.culture.group.stratum

import io.tashtabash.random.*
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomUnwrappedElementOrNull
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.RequestConstructController
import io.tashtabash.sim.culture.group.place.MovablePlace
import io.tashtabash.sim.culture.group.request.RequestPool
import io.tashtabash.sim.culture.group.request.RequestType
import io.tashtabash.sim.culture.group.request.resourceToRequest
import io.tashtabash.sim.space.territory.Territory
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.TileTag
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

        allGoodProduced.randomUnwrappedElementOrNull()?.let { chosen ->
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
