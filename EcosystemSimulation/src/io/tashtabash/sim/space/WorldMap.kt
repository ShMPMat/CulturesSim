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


class WorldMap(val linedTiles: List<List<Tile>>) {
    val maxX = linedTiles.size
    val maxY = linedTiles[0].size

    init {
        val ids = linedTiles.flatten().map { it.id }
        require(ids.size == ids.distinct().size) { "Tile ids must be unique" }
    }

    val tectonicPlates = mutableListOf<TectonicPlate>()

    fun addPlate(plate: TectonicPlate) {
        tectonicPlates.add(plate)
    }

    operator fun get(_x: Int, _y: Int): Tile? {
        var curX = _x
        var curY = _y

        if (data.xMapLooping)
            curX = cutCoordinate(curX, maxX)
        else if (!checkCoordinate(curX, maxX))
            return null

        if (data.yMapLooping)
            curY = cutCoordinate(curY, maxY)
        else if (!checkCoordinate(curY, maxY))
            return null

        return linedTiles[curX][curY]
    }

    fun getValue(_x: Int, _y: Int) = get(_x, _y)!!

    private fun cutCoordinate(coordinate: Int, max: Int): Int {
        var curCoordinate = coordinate

        if (curCoordinate < 0)
            curCoordinate = curCoordinate % max + max

        return curCoordinate % max
    }

    private fun checkCoordinate(coordinate: Int, max: Int) = coordinate in 0 until max

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

    val tiles = linedTiles.flatten()

    fun getTiles(predicate: (Tile) -> Boolean) = linedTiles
            .flatten()
            .filter(predicate)

    fun update() {
        if (tileUpdateOrder == null) {
            for (line in linedTiles)
                for (tile in line)
                    tile.startUpdate()

            for (line in linedTiles)
                for (tile in line)
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

    private val middleUpdateOrder: List<List<TilesBatch>> = listOf(linedTiles)

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
        for (line in linedTiles)
            for (tile in line)
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
    // Returns a TileBatch List per colour (up to 4), each TileBatch List can be handled async
    fun calculateTileUpdateOrder(margin: Int): List<List<TilesBatch>> {
        require(margin > 0) { "Margin must be positive, got $margin" }

        val xBlocks = splitIntoBlocks(maxX, 2 * margin, data.xMapLooping)
        val yBlocks = splitIntoBlocks(maxY, 2 * margin, data.yMapLooping)

        return (0 until 4).map { colourIdx ->
            xBlocks.filterIndexed { i, _ -> i % 2 == colourIdx / 2 }.flatMap { xRange ->
                yBlocks.filterIndexed { j, _ -> j % 2 == colourIdx % 2 }.map { yRange ->
                    xRange.flatMap { x -> yRange.map { y -> linedTiles[x][y] } }
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
        for (line in linedTiles)
            for (tile in line)
                tile.levelUpdate()

        platesUpdate()
    }

    fun platesUpdate() {
        for (plate in tectonicPlates)
            plate.move()
    }
}
