package io.tashtabash.random.singleton

import io.tashtabash.random.randomTile
import io.tashtabash.random.randomTileOnBrink
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.territory.Territory
import io.tashtabash.sim.space.tile.Tile


fun Territory.randomTile() = randomTile(this)

fun WorldMap.randomTile(): Tile = randomTile(this)

fun Collection<Tile>.randomTileOnBrink(predicate: (Tile) -> Boolean) =
        randomTileOnBrink(this, predicate)
