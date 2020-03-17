package shmp.random

import simulation.space.Territory
import simulation.space.tile.Tile
import simulation.space.WorldMap
import kotlin.random.Random

/**
 * @param territory A Territory from which a random Tile will be chosen.
 * @return A random Tile from the Territory.
 */
fun randomTile(territory: Territory, random: Random): Tile {
    return randomElement(territory.tiles, random)
}

fun randomTile(map: WorldMap, random: Random): Tile {
    return randomElement(randomElement(map.tiles, random), random)
}