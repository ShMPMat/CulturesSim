package io.tashtabash.sim.space

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.tile.Direction
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.setTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.random.Random


class WorldMap(val tiles: List<Tile>, val maxX: Int, val maxY: Int) {
    // The Tiles of each cell, in the order of tiles
    private val cells: List<List<List<Tile>>>

    init { // Cache tiles into a grid
        val ids = tiles.map { it.id }
        require(ids.size == ids.distinct().size) { "Tile ids must be unique" }

        val newCells = List(maxX) { List(maxY) { mutableListOf<Tile>() } }
        for (tile in tiles) {
            require(tile.x in 0 until maxX && tile.y in 0 until maxY) {
                "Tile ${tile.id} at ${tile.posStr} is outside of the map"
            }
            newCells[tile.x][tile.y] += tile
        }
        require(newCells.all { line -> line.all { it.isNotEmpty() } }) { "Every cell must have a Tile" }

        cells = newCells
    }

    val lines: List<List<Tile>> = cells.map { it.flatten() }

    val tectonicPlates = mutableListOf<TectonicPlate>()

    fun addPlate(plate: TectonicPlate) {
        tectonicPlates.add(plate)
    }

    fun tilesAt(x: Int, y: Int): List<Tile> {
        val curX = cutCoordinate(x, maxX, data.xMapLooping)
            ?: return listOf()
        val curY = cutCoordinate(y, maxY, data.yMapLooping)
            ?: return listOf()

        return cells[curX][curY]
    }

     // Only for coordinates which can't hold several Tiles
    operator fun get(x: Int, y: Int): Tile? {
        val cellTiles = tilesAt(x, y)
        check(cellTiles.size <= 1) { "Cell $x $y has ${cellTiles.size} Tiles, use tilesAt(..)" }

        return cellTiles.firstOrNull()
    }

    fun getValue(x: Int, y: Int) = get(x, y)
        ?: throw IllegalArgumentException("Tile $x $y has no tiles")

    fun getMainTile(x: Int, y: Int): Tile? =
        tilesAt(x, y).maxByOrNull { it.area }

    private fun cutCoordinate(coordinate: Int, max: Int, isLooping: Boolean): Int? {
        if (!isLooping)
            return coordinate.takeIf { it in 0 until max }

        var curCoordinate = coordinate

        if (curCoordinate < 0)
            curCoordinate = curCoordinate % max + max

        return curCoordinate % max
    }

    /**
     * @return the Direction in which `to` lies from `from`, taking map looping into account;
     * null if `to` isn't adjacent to `from`.
     */
    fun direction(from: Tile, to: Tile): Direction? = Direction.of(
        shortestOffset(to.x - from.x, maxX, data.xMapLooping),
        shortestOffset(to.y - from.y, maxY, data.yMapLooping)
    )

    // On a looping axis, picks the shorter way around
    private fun shortestOffset(offset: Int, max: Int, isLooping: Boolean) = when {
        !isLooping -> offset
        offset > max / 2 -> offset - max
        offset < -max / 2 -> offset + max
        else -> offset
    }

    fun setTags() {
        var name = 0
        val allTiles = tiles

        for (tile in allTiles)
            if (setTags(tile, "" + name))
                name++
    }

    fun getTiles(predicate: (Tile) -> Boolean) = tiles.filter(predicate)

    fun update() {
        if (tileUpdateOrder == null) {
            for (tile in tiles)
                tile.startUpdate()

            for (tile in tiles)
                tile.middleUpdate()
        } else tileUpdateOrder?.let {
            runOnMap(it) { tile ->
                tile.startUpdate()
            }
            runOnMap(middleUpdateOrder) { tile ->
                tile.middleUpdate()
            }
        }
    }

    private val middleUpdateOrder: List<List<TilesBatch>> = listOf(lines)

    inline fun runOnMap(order: List<List<TilesBatch>>, crossinline operation: (Tile) -> Unit) {
        val seeds = order.map { batchGroup -> batchGroup.map { RandomSingleton.random.nextLong() } }

        runBlocking(Dispatchers.Default) {
            for ((batchGroup, groupSeeds) in order.zip(seeds))
                batchGroup.zip(groupSeeds).map { (batch, seed) ->
                    async {
                        RandomSingleton.withRandom(Random(seed)) {
                            for (tile in batch)
                                operation(tile)
                        }
                    }
                }.awaitAll()
        }
    }

    fun finishUpdate() {
        for (tile in tiles)
            tile.finishUpdate()
    }

    private typealias TilesBatch = List<Tile>
    var tileUpdateOrder: List<List<TilesBatch>>? = null

    // Splits the map into blocks at least 2 * margin wide and colours them as a 2x2 checkerboard.
    // Like this
    // 1 2 1 2
    // 3 4 3 4
    // 1 2 1 2
    // 3 4 3 4
    // Blocks of the same colour are separated by a whole block of another colour, so the areas
    // within (margin - 1) of two same-coloured blocks never intersect.
    // All Tiles of a cell get into the same block.
    // Returns a TileBatch List per colour (up to 4), each TileBatch List can be handled async
    fun calculateTileUpdateOrder(margin: Int): List<List<TilesBatch>> {
        require(margin > 0) { "Margin must be positive, got $margin" }

        val xBlocks = splitIntoBlocks(maxX, 2 * margin, data.xMapLooping)
        val yBlocks = splitIntoBlocks(maxY, 2 * margin, data.yMapLooping)

        return (0 until 4).map { colourIdx ->
            xBlocks.filterIndexed { i, _ -> i % 2 == colourIdx / 2 }.flatMap { xRange ->
                yBlocks.filterIndexed { j, _ -> j % 2 == colourIdx % 2 }.map { yRange ->
                    xRange.flatMap { x -> yRange.flatMap { y -> cells[x][y] } }
                }
            }
        }.filter { it.isNotEmpty() }
    }

    // Splits map dimensions into consecutive ranges at least minWidth wide.
    // On a looping axis the number of ranges must be even, otherwise the first and
    // the last ranges would get the same colour while being adjacent through the loop.
    private fun splitIntoBlocks(size: Int, minSize: Int, isLooping: Boolean): List<IntRange> {
        var numberOfRanges = max(1, size / minSize)
        if (isLooping && numberOfRanges % 2 == 1 && numberOfRanges > 1)
            numberOfRanges--

        val baseWidth = size / numberOfRanges
        val remainder = size % numberOfRanges
        var start = 0

        return List(numberOfRanges) { i ->
            val width = baseWidth + if (i < remainder) 1 else 0

            (start until start + width)
                .also { start += width }
        }
    }

    fun geologicUpdate() {
        for (tile in tiles)
            tile.levelUpdate()

        platesUpdate()
    }

    fun platesUpdate() {
        for (plate in tectonicPlates)
            plate.move()
    }
}
