package io.tashtabash.sim.space

import io.tashtabash.sim.space.tile.Direction
import io.tashtabash.sim.space.tile.Tile
import kotlin.math.abs


// The whole world in cells of one Scale
class Extent(val sizeX: Int, val sizeY: Int, val isXLooping: Boolean, val isYLooping: Boolean) {
    init {
        require(sizeX > 0 && sizeY > 0) { "Got empty extent: ${sizeX}x$sizeY" }
    }

    // The coordinate in 0 until size, wrapped on a looping axis; null outside a non-looping one
    fun cutX(x: Int) = cut(x, sizeX, isXLooping)
    fun cutY(y: Int) = cut(y, sizeY, isYLooping)

    // The shorter way around on a looping axis
    fun offsetX(from: Int, to: Int) = shortestOffset(to - from, sizeX, isXLooping)
    fun offsetY(from: Int, to: Int) = shortestOffset(to - from, sizeY, isYLooping)

    fun direction(from: Tile, to: Tile): Direction? = Direction.of(offsetX(from.x, to.x), offsetY(from.y, to.y))

    fun distance(from: Tile, to: Tile) = abs(offsetX(from.x, to.x)) + abs(offsetY(from.y, to.y))

    // The same world in cells `factor` times larger
    fun coarsen(factor: Int): Extent {
        require(factor >= 1 && sizeX % factor == 0 && sizeY % factor == 0) {
            "Extent ${sizeX}x$sizeY can't be condensed by $factor, its dimensions aren't divisible"
        }

        return Extent(sizeX / factor, sizeY / factor, isXLooping, isYLooping)
    }

    override fun toString() = "Extent ${sizeX}x$sizeY"
}

private fun cut(coordinate: Int, max: Int, isLooping: Boolean): Int? {
    if (!isLooping)
        return coordinate.takeIf { it in 0 until max }

    var curCoordinate = coordinate

    if (curCoordinate < 0)
        curCoordinate = curCoordinate % max + max

    return curCoordinate % max
}

private fun shortestOffset(offset: Int, max: Int, isLooping: Boolean) = when {
    !isLooping -> offset
    offset > max / 2 -> offset - max
    offset < -max / 2 -> offset + max
    else -> offset
}
