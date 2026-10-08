package io.tashtabash.sim.space

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.space.tile.Direction
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.setTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.random.Random


class WorldMap(
    val tiles: List<Tile>, // Have the global coordinates of the Extent
    val extent: Extent,
    val scale: Scale,
    val coveredRegion: Region = Region(0, 0, extent.sizeX, extent.sizeY)
) {
    // The Tiles of each cell stored in local coordinates
    private val cells: List<List<List<Tile>>>

    init {
        val ids = tiles.map { it.id }
        require(ids.size == ids.distinct().size) { "Tile ids must be unique" }
        require(isRectangleInside(coveredRegion.x, coveredRegion.sizeX, extent.sizeX, extent.isXLooping)
                && isRectangleInside(coveredRegion.y, coveredRegion.sizeY, extent.sizeY, extent.isYLooping)) {
            "Map $coveredRegion isn't inside the $extent"
        }
        require(tiles.all { it.scale == scale }) { "All Tiles must have the same scale $scale" }

        // Cache tiles into a grid
        val newCells = List(coveredRegion.sizeX) { List(coveredRegion.sizeY) { mutableListOf<Tile>() } }
        for (tile in tiles) {
            require(extent.cutX(tile.x) == tile.x && extent.cutY(tile.y) == tile.y) {
                "Tile ${tile.id} at ${tile.posStr} has coordinates outside of the $extent"
            }
            val x = localX(tile.x)
            val y = localY(tile.y)
            require(x != null && y != null) { "Tile ${tile.id} at ${tile.posStr} is outside of the map" }
            newCells[x][y] += tile
        }
        require(newCells.all { line -> line.all { it.isNotEmpty() } }) { "Every cell must have a Tile" }

        cells = newCells
    }

    // The global coordinates of the covered cells
    val xCoordinates: List<Int> = (0 until coveredRegion.sizeX).map { extent.cutX(coveredRegion.x + it)!! }
    val yCoordinates: List<Int> = (0 until coveredRegion.sizeY).map { extent.cutY(coveredRegion.y + it)!! }

    val lines: List<List<Tile>> = cells.map { it.flatten() }

    val tectonicPlates = mutableListOf<TectonicPlate>()

    fun addPlate(plate: TectonicPlate) {
        tectonicPlates += plate
    }

    // The coordinate in the local coordinate space
    private fun localX(x: Int) = local(extent.cutX(x), coveredRegion.x, this@WorldMap.coveredRegion.sizeX, extent.sizeX)
    private fun localY(y: Int) = local(extent.cutY(y), coveredRegion.y, this@WorldMap.coveredRegion.sizeY, extent.sizeY)

    private fun local(coordinate: Int?, origin: Int, size: Int, extentSize: Int): Int? {
        coordinate ?: return null

        return (coordinate - origin).mod(extentSize)
            .takeIf { it < size }
    }

    fun isCovered(x: Int, y: Int) = localX(x) != null && localY(y) != null

    // Empty for the cells which aren't covered
    fun getTilesAt(x: Int, y: Int): List<Tile> {
        val localX = localX(x)
            ?: return listOf()
        val localY = localY(y)
            ?: return listOf()

        return cells[localX][localY]
    }

     // Only for coordinates which can't hold several Tiles
    operator fun get(x: Int, y: Int): Tile? {
        val cellTiles = getTilesAt(x, y)
        check(cellTiles.size <= 1) { "Cell $x $y has ${cellTiles.size} Tiles, use tilesAt(..)" }

        return cellTiles.firstOrNull()
    }

    fun getValue(x: Int, y: Int) = get(x, y)
        ?: throw IllegalArgumentException("Tile $x $y has no tiles")

    fun getMainTile(x: Int, y: Int): Tile? =
        getTilesAt(x, y).maxByOrNull { it.area }


    fun direction(from: Tile, to: Tile): Direction? = extent.direction(from, to)

    fun distance(from: Tile, to: Tile) = extent.distance(from, to)

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

        // The map loops only along an axis it covers completely
        val xBlocks = splitIntoBlocks(
            coveredRegion.sizeX,
            2 * margin,
            extent.isXLooping && coveredRegion.sizeX == extent.sizeX
        )
        val yBlocks = splitIntoBlocks(
            coveredRegion.sizeY,
            2 * margin,
            extent.isYLooping && coveredRegion.sizeY == extent.sizeY
        )

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

// Whether the range starting at the origin fits into the extent; it can cross the edge of a looping axis
private fun isRectangleInside(origin: Int, size: Int, extentSize: Int, isLooping: Boolean) =
    origin in 0 until extentSize && size in 1..extentSize && (isLooping || origin + size <= extentSize)
