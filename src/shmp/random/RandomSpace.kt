package shmp.random

import simulation.space.Territory
import simulation.space.Tile
import simulation.space.WorldMap
import java.util.function.Predicate
import kotlin.random.Random

/**
 *
 * @param tiles any set of tiles.
 * @param predicate predicate which tiles on brink will satisfy.
 * @return random Tile on brink of tiles, which satisfies predicate.
 * If such Tile does not exists, returns null.
 */
fun randomTileOnBrink(tiles: Collection<Tile>, predicate: Predicate<Tile>, random: Random): Tile {
    val brink = Territory(tiles).getBrinkWithCondition(predicate)
    return if (brink.isEmpty())
        throw RandomException("Tiles brink is empty")
    else
        randomElement(brink, random)
}

/**
 * @param territory A Territory from which a random Tile will be chosen.
 * @return A random Tile from the Territory.
 */
fun randomTile(territory: Territory, random: Random): Tile {
    return randomElement(territory.tiles, random)
}

fun randomTile(map: WorldMap, random: Random): Tile {
    return randomElement(randomElement(map.map, random), random)
}