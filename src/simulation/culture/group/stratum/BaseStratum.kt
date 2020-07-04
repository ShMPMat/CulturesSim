package simulation.culture.group.stratum

import shmp.random.randomElement
import simulation.Controller
import simulation.culture.group.centers.Group
import simulation.culture.group.place.StaticPlace
import simulation.culture.group.request.resourceToRequest
import simulation.space.Territory
import simulation.space.resource.Resource
import simulation.space.resource.container.MutableResourcePack
import simulation.space.tile.Tile
import simulation.space.tile.TileTag

abstract class BaseStratum(tile: Tile, protected val name: String) : Stratum {
    override val ego = Ego(tile, name)

    protected val innerPlaces = mutableListOf<StaticPlace>()
    override val places: List<StaticPlace>
        get() = innerPlaces

    override fun update(accessibleResources: MutableResourcePack, accessibleTerritory: Territory, group: Group) {
        places.forEach { place ->
            val lacking = place.getLacking()
            val gotResources = lacking
                    .map { resourceToRequest(it, group, it.amount, 50) }
                    .flatMap { group.populationCenter.executeRequest(it).pack.resources }
            place.addResources(gotResources)
        }
    }

    protected fun addEnhancement(resource: Resource, group: Group) {
        val goodPlaces = innerPlaces.filter { resource.genome.isAcceptable(it.tile) }
        var place: StaticPlace? = null

        if (goodPlaces.isEmpty()) {
            val goodTiles = group.territoryCenter.territory
                    .getTiles { resource.genome.isAcceptable(it) }
            if (goodTiles.isNotEmpty()) {
                val tagType = "($name of ${group.name})"
                place = StaticPlace(
                        randomElement(goodTiles, Controller.session.random),
                        TileTag(tagType + "_" + innerPlaces.size, tagType)
                )
                innerPlaces.add(place)
            }
        } else
            place = randomElement(innerPlaces, Controller.session.random)

        if (place == null) return

        place.addResource(resource)
    }

    override fun toString() = "\nEgo resources: ${ego.place.current.owned}"
}
