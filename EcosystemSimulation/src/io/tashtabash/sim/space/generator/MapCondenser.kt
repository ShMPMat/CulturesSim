package io.tashtabash.sim.space.generator

import io.tashtabash.sim.space.Scale
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.tile.MOUNTAIN_LEVEL
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.updater.TileUpdater
import kotlin.math.roundToInt


/**
 * Condenses the fine map into a map with cells `factor` times larger.
 * Tiles on one coordinate are neighbours with each other and all tiles in the 4 directions
 */
fun condenseMap(
    fine: WorldMap,
    factor: Int,
    createUpdaters: () -> MutableList<TileUpdater>,
    sliverThreshold: Double = .0 // If a Tile Type takes lesser than this fraction, these Tiles will be discarded
): WorldMap {
    require(listOf(fine.originX, fine.originY, fine.maxX, fine.maxY).all { it % factor == 0 }) {
        "${fine.originX} ${fine.originY} ${fine.maxX}x${fine.maxY} can't be condensed by $factor, " +
                "its origin & dimensions aren't divisible"
    }
    val region = Region(fine.originX / factor, fine.originY / factor, fine.maxX / factor, fine.maxY / factor)

    return condenseRegion(fine, factor, region, createUpdaters, sliverThreshold, moveResources = true)
}

class Region(val x: Int, val y: Int, val sizeX: Int, val sizeY: Int) {
    init {
        require(sizeX > 0 && sizeY > 0) { "Region must have cells, got ${sizeX}x$sizeY" }
    }
}

fun condenseRegion(
    fine: WorldMap,
    factor: Int,
    region: Region, // Can cross the edge of a looping axis
    createUpdaters: () -> MutableList<TileUpdater>,
    sliverThreshold: Double = .0,
    moveResources: Boolean = false
): WorldMap {
    require(sliverThreshold in 0.0..0.5) { "Sliver threshold must be in 0..0.5, got $sliverThreshold" }

    val fineScale = fine.scale
    val coarseScale = Scale(fineScale.tileSizeKm * factor, fineScale.tickDurationSeconds)
    val extent = fine.extent.coarsen(factor)
    val condensedTiles = mutableListOf<Tile>()
    for (i in 0 until region.sizeX)
        for (j in 0 until region.sizeY) {
            val x = extent.cutX(region.x + i)
            val y = extent.cutY(region.y + j)
            require(x != null && y != null) { "Region isn't inside the $extent" }

            val cellTiles = (x * factor until (x + 1) * factor).flatMap { fineX ->
                (y * factor until (y + 1) * factor).flatMap { fineY -> fine.getTilesAt(fineX, fineY) }
            }
            require(cellTiles.isNotEmpty()) { "The fine map doesn't cover the cell $x $y" }

            val split = splitCell(cellTiles, sliverThreshold)
            for (partTiles in split) {
                // A single part covers the whole cell, slivers included
                val allTileTiles = if (split.size == 1) partTiles.copy(tiles = cellTiles) else partTiles
                val condensedTile = createPart(condensedTiles.size, x, y, allTileTiles, coarseScale, createUpdaters)
                if (moveResources)
                    moveResources(allTileTiles.tiles, condensedTile)
                condensedTiles += condensedTile
            }
        }

    val map = WorldMap(condensedTiles, extent, coarseScale, region.x, region.y, region.sizeX, region.sizeY)
    linkTiles(map)

    return map
}

private data class PartTiles(val tiles: List<Tile>, val isWater: Boolean)

private fun Tile.isWater() = type == Tile.Type.Water || type == Tile.Type.Ice

private fun splitCell(tiles: List<Tile>, sliverThreshold: Double): List<PartTiles> {
    val (water, land) = tiles.partition { it.isWater() }
    val waterShare = water.sumOf { it.area } / tiles.sumOf { it.area }

    return when {
        waterShare <= sliverThreshold -> listOf(PartTiles(land, false))
        1 - waterShare <= sliverThreshold -> listOf(PartTiles(water, true))
        else -> listOf(PartTiles(land, false), PartTiles(water, true))
    }
}

private fun createPart(
    id: Int,
    x: Int,
    y: Int,
    partTiles: PartTiles,
    scale: Scale,
    createUpdaters: () -> MutableList<TileUpdater>
): Tile {
    val tiles = partTiles.tiles
    val tilesArea = tiles.sumOf { it.area }
    val level = (tiles.sumOf { it.level * it.area } / tilesArea).roundToInt()
    val secondLevel = (tiles.sumOf { it.secondLevel * it.area } / tilesArea).roundToInt()
    val area = tiles.sumOf { it.area }
    val type = when {
        partTiles.isWater -> Tile.Type.Water
        level >= MOUNTAIN_LEVEL -> Tile.Type.Mountain
        else -> Tile.Type.Normal
    }

    return Tile(id, x, y, createUpdaters(), scale, area)
        .apply { setTerrain(type, level, secondLevel) }
}

private fun moveResources(tiles: List<Tile>, part: Tile) {
    for (tile in tiles)
        for (resource in tile.resourcesWithMoved)
            part.addDelayedResource(resource)
}

private fun linkTiles(map: WorldMap) {
    for (x in map.xCoordinates)
        for (y in map.yCoordinates)
            for (tile in map.getTilesAt(x, y)) {
                val adjacentCells = listOf(x to y + 1, x to y - 1, x + 1 to y, x - 1 to y)
                val neighbours = (adjacentCells.flatMap { (nx, ny) -> map.getTilesAt(nx, ny) } + map.getTilesAt(x, y))
                    .distinct()
                    .filter { it != tile }

                tile.setNeighbours(neighbours.map { it to map.direction(tile, it)!! })
            }
}
