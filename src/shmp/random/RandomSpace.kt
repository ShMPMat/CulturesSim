package shmp.random

import simulation.space.territory.Territory
import simulation.space.WorldMap
import simulation.space.territory.StaticTerritory
import simulation.space.tile.Tile
import kotlin.random.Random

fun randomTile(territory: Territory, random: Random) = randomElement(territory.tiles.toList(), random)

fun randomTile(map: WorldMap, random: Random): Tile = randomElement(randomElement(map.linedTiles, random), random)

/**
 *
 * @return random Tile on brink of tiles, which satisfies predicate.
 * If such Tile does not exists, returns null.
 */
fun randomTileOnBrink(tiles: Collection<Tile>, random: Random, predicate: (Tile) -> Boolean): Tile? {
    val brink = StaticTerritory(tiles).filterOuterBrink(predicate)
    return if (brink.isNotEmpty())
        randomElement(brink, random)
    else null
}
