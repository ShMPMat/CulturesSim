package io.tashtabash.sim.space.tile


data class LandscapeTile(val level: Short, val type: Byte, val plateIdx: Byte) {
    fun applyToTile(tile: Tile) {
        tile.setType(extractType(), false)
        tile.setLevel(level.toInt())
        // No need to set the plate to the refined Tiles
    }

    fun extractType() =
        Tile.Type.entries[type.toInt()]
}
