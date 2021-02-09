package shmp.simulation.culture.group.stratum

import shmp.random.randomElement
import shmp.simulation.Controller
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.place.StaticPlace
import shmp.simulation.culture.group.request.resourceToRequest
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.tile.Tile
import shmp.simulation.space.tile.TileTag


abstract class BaseStratum(tile: Tile, final override val name: String) : Stratum {
    override val ego = Ego(tile, name)

    protected val innerPlaces = mutableListOf<StaticPlace>()
    override val places: List<StaticPlace>
        get() = innerPlaces

    override fun update(accessibleResources: MutableResourcePack, accessibleTerritory: Territory, group: Group) {
        places.forEach { place ->
            val lacking = place.getLacking()
            val gotResources = lacking
                    .map { resourceToRequest(it, group, it.amount, 50, setOf()) }
                    .flatMap { group.populationCenter.executeRequest(it).pack.resources }
            place.addResources(gotResources)
        }
    }

    protected fun addEnhancement(resource: Resource, group: Group) {
        val goodPlaces = innerPlaces.filter { resource.isAcceptable(it.tile) }
        var place: StaticPlace? = null

        if (goodPlaces.isEmpty()) {
            val goodTiles = group.territoryCenter.territory
                    .filter { resource.isAcceptable(it) }


            if (goodTiles.isNotEmpty()) {
                val tagType = "($name of ${group.name})"
                place = StaticPlace(
                        randomElement(goodTiles, Controller.session.random),
                        TileTag(tagType + "_" + innerPlaces.size, tagType)
                )
                addPlace(place)
            }
        } else
            place = randomElement(goodPlaces, Controller.session.random)

        if (place == null)
            return

        place.addResource(resource)
    }

    fun addPlace(place: StaticPlace) = innerPlaces.add(place)

    override fun toString() = """
        |
        |Ego resources:
        |${ego.place.current.owned}
    """.trimMargin()
}
