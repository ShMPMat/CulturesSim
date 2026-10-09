package io.tashtabash.sim.space.generator

import io.tashtabash.sim.space.Extent
import io.tashtabash.sim.space.Region
import io.tashtabash.sim.space.Scale
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.tile.MOUNTAIN_LEVEL
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.updater.TileUpdater
import kotlin.math.roundToInt


data class LandscapeData(
    val coveredRegion: Region,
    val scale: Scale,
    val extent: Extent,
    val getTilesAt: (x: Int, y: Int) -> List<Tile>
)

/**
 * Condenses the fine map into a map with cells `factor` times larger.
 * Tiles on one coordinate are neighbours with each other and all tiles in the 4 directions
 */
fun condenseMap(
    fine: LandscapeData,
    factor: Int,
    createUpdaters: () -> MutableList<TileUpdater>,
    sliverThreshold: Double = .0 // If a Tile Type takes lesser than this fraction, these Tiles will be discarded
): WorldMap {
    require(listOf(fine.coveredRegion.x, fine.coveredRegion.y, fine.coveredRegion.sizeX, fine.coveredRegion.sizeY)
        .all { it % factor == 0 }) {
            "${fine.coveredRegion} can't be condensed by $factor, its origin & dimensions aren't divisible"
        }
    val region = fine.coveredRegion / factor

    return condenseRegion(fine, factor, region, createUpdaters, sliverThreshold, moveResources = true)
}

fun condenseRegion(
    fine: LandscapeData,
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

    val map = WorldMap(condensedTiles, extent, coarseScale, region)
    linkTiles(map)

    return map
}

private data class PartTiles(val tiles: List<Tile>, val isWater: Boolean)

fun Tile.isWater() = type == Tile.Type.Water || type == Tile.Type.Ice

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
