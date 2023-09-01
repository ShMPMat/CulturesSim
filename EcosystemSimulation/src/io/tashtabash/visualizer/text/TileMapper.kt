package io.tashtabash.visualizer.text

import io.tashtabash.sim.space.tile.Tile


data class TileMapper(val mapper: (Tile) -> String, val order: Int, val name: String = "Unnamed")
