package shmp.random

import shmp.simulation.space.territory.Territory
import shmp.simulation.space.WorldMap
import shmp.simulation.space.territory.StaticTerritory
import shmp.simulation.space.tile.Tile
import kotlin.random.Random


fun randomTile(territory: Territory, random: Random) = randomElement(territory.tiles.toList(), random)

fun randomTile(map: WorldMap, random: Random): Tile = randomElement(randomElement(map.linedTiles, random), random)

/**
 *
 * @return random Tile on brink of tiles, which satisfies predicate.
 * If such Tile does not exists, returns null.
 */
fun randomTileOnBrink(tiles: Collection<Tile>, random: Random, predicate: (Tile) -> Boolean): Tile? =
        randomElementOrNull(
                StaticTerritory(tiles).filterOuterBrink(predicate),
                random
        )
