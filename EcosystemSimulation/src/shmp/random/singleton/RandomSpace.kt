package shmp.random.singleton

import shmp.random.randomTile
import shmp.random.randomTileOnBrink
import shmp.simulation.space.WorldMap
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.tile.Tile


fun Territory.randomTile() = randomTile(this)

fun WorldMap.randomTile(): Tile = randomTile(this)

fun Collection<Tile>.randomTileOnBrink(predicate: (Tile) -> Boolean) =
        randomTileOnBrink(this, predicate)
