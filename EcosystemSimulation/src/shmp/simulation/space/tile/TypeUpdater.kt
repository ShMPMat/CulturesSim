package shmp.simulation.space.tile

import shmp.simulation.space.SpaceData
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.ResourceType


class TypeUpdater(val water: Resource) {
    fun updateType(tile: Tile) {
        if (tile.resourcePack.contains(water))
            tile.setType(Tile.Type.Water, false)
        else if (tile.type in listOf(Tile.Type.Normal, Tile.Type.Woods, Tile.Type.Growth)) when {
            tile.resourcePack.any { it.tags.any { t -> t.name == "Tree" } } ->
                tile.setType(Tile.Type.Woods, false)
            tile.resourcePack.any { it.genome.type === ResourceType.Plant } ->
                tile.setType(Tile.Type.Growth, false)
            else -> tile.setType(Tile.Type.Normal, false)
        } else if (tile.type == Tile.Type.Water && tile.temperature < -10)
            tile.setType(Tile.Type.Ice, true)
        else if (tile.type == Tile.Type.Ice && tile.temperature > 0) {
            tile.type = Tile.Type.Water
            tile.level = tile.secondLevel
            tile.addDelayedResource(SpaceData.data.resourcePool.getBaseName("SaltWater"))
        }
    }
}
