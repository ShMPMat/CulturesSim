package io.tashtabash.sim.culture.group.stratum

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.place.StaticPlace
import io.tashtabash.sim.culture.group.request.resourceToRequest
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.territory.Territory
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.TileTag


abstract class BaseStratum(tile: Tile, final override val baseName: String, postfix: String) : Stratum {
    final override val name = baseName + postfix
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
        val goodPlaces = innerPlaces.filter { resource.areNecessaryDependenciesSatisfied(it.tile) }
        var place: StaticPlace? = null

        if (goodPlaces.isEmpty()) {
            val goodTiles = group.territoryCenter.territory
                    .filter { resource.areNecessaryDependenciesSatisfied(it) }


            if (goodTiles.isNotEmpty()) {
                val tagType = "($name of ${group.name})"
                place = StaticPlace(
                        goodTiles.randomElement(),
                        TileTag(tagType + "_" + innerPlaces.size, tagType)
                )
                addPlace(place)
            }
        } else
            place = goodPlaces.randomElement()

        if (place == null)
            return

        place.addResource(resource)
    }

    fun addPlace(place: StaticPlace) = innerPlaces.add(place)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseStratum) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString() = """
        |
        |Ego resources:
        |${ego.place.current.owned}
    """.trimMargin()
}
