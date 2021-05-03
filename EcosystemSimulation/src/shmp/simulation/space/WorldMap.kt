package shmp.simulation.space

import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.tile.Tile
import shmp.simulation.space.tile.setTags


class WorldMap(val linedTiles: List<List<Tile>>) {
    val maxX = linedTiles.size
    val maxY = linedTiles[0].size

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

    private fun cutCoordinate(coordinate: Int, max: Int): Int {
        var curCoordinate = coordinate

        if (curCoordinate < 0)
            curCoordinate = curCoordinate % max + max

        return curCoordinate % max
    }

    private fun checkCoordinate(coordinate: Int, max: Int) = coordinate in 0 until max

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

    @Synchronized
    fun update() { //TODO parallel
        for (line in linedTiles)
            for (tile in line)
                tile.startUpdate()

        for (line in linedTiles)
            for (tile in line)
                tile.middleUpdate(this)
    }

    @Synchronized
    fun finishUpdate() {
        for (line in linedTiles)
            for (tile in line)
                tile.finishUpdate()
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
