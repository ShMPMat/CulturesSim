package shmp.visualizer.text

import shmp.simulation.space.tile.Tile

data class TileMapper(val mapper: (Tile) -> String, val order: Int)
