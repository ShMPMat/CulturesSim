package simulation.space.tile

import simulation.space.resource.Genome
import simulation.space.resource.Resource

class TypeUpdater(val water: Resource) {
    fun updateType(tile: Tile) {
        if (tile.resourcePack.contains(water))
            tile.setType(Tile.Type.Water, false)
        else if (tile.type in listOf(Tile.Type.Normal, Tile.Type.Woods, Tile.Type.Growth)) when {
            tile.resourcePack.any { it.simpleName.matches(".*Tree|Spruce".toRegex()) } ->
                tile.setType(Tile.Type.Woods, false)
            tile.resourcePack.any { it.genome.type === Genome.Type.Plant } ->
                tile.setType(Tile.Type.Growth, false)
            else -> tile.setType(Tile.Type.Normal, false)
        } else if (tile.type == Tile.Type.Water && tile.temperature < -10) {
            tile.setType(Tile.Type.Ice, true)
        } else if (tile.type == Tile.Type.Ice && tile.temperature > 0) {
            tile.type = Tile.Type.Water
            tile.level = tile.secondLevel
        }
    }
}