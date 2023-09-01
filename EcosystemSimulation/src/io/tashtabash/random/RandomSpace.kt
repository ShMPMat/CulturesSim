package io.tashtabash.random

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.sim.space.territory.Territory
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.territory.StaticTerritory
import io.tashtabash.sim.space.tile.Tile


fun randomTile(territory: Territory) = territory.tiles.sortedBy { it.x * 1000000 + it.y }.randomElementOrNull()

fun randomTile(map: WorldMap): Tile = map.linedTiles.randomElement().randomElement()

/**
 *
 * @return random Tile on brink of tiles, which satisfies predicate.
 * If such Tile does not exists, returns null.
 */
fun randomTileOnBrink(tiles: Collection<Tile>, predicate: (Tile) -> Boolean): Tile? =
        StaticTerritory(tiles.toSet()).filterOuterBrink(predicate).randomElementOrNull()
