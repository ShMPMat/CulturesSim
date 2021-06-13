package shmp.random

import shmp.random.singleton.randomElement
import shmp.random.singleton.randomElementOrNull
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.WorldMap
import shmp.simulation.space.territory.StaticTerritory
import shmp.simulation.space.tile.Tile


fun randomTile(territory: Territory) = territory.tiles.sortedBy { it.x * 1000000 + it.y }.randomElementOrNull()

fun randomTile(map: WorldMap): Tile = map.linedTiles.randomElement().randomElement()

/**
 *
 * @return random Tile on brink of tiles, which satisfies predicate.
 * If such Tile does not exists, returns null.
 */
fun randomTileOnBrink(tiles: Collection<Tile>, predicate: (Tile) -> Boolean): Tile? =
        StaticTerritory(tiles.toSet()).filterOuterBrink(predicate).randomElementOrNull()
