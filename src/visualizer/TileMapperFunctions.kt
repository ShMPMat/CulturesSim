package visualizer

import simulation.space.TectonicPlate
import simulation.space.tile.Tile
import java.util.*
import java.util.stream.Collectors
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

fun vapourMapper(tile: Tile): String {
    val level = tile.resourcesWithMoved
            .filter { it.simpleName == "Vapour" }
            .map { it.amount }.fold(0, Int::plus)
    var colour = when {
        level == 0 -> "\u001b[44m"
        level < 50 -> "\u001b[104m"
        level < 100 -> "\u001b[46m"
        level < 150 -> "\u001b[47m"
        level < 200 -> "\u001b[43m"
        else -> "\u001b[41m"
    }
    return "\u001b[90m" + colour + level / 10 % 10
}

fun platesMapper(plates: List<TectonicPlate>, tile: Tile): String {
    val affectedTiles: MutableList<Tile> = ArrayList()
    plates.forEach { plate ->
        affectedTiles += plate.affectedTiles.map { it.first }
    }
    val (plate, ord) = plates.zip(plates.indices).find { it.first.contains(tile) }
            ?: return " "
    if (affectedTiles.contains(tile))
        return "\u001b[" + (30 + ord) + "mX"
    val direction = when (plate.direction) {
        TectonicPlate.Direction.D -> "v"
        TectonicPlate.Direction.L -> "<"
        TectonicPlate.Direction.R -> ">"
        TectonicPlate.Direction.U -> "^"
        else -> throw RuntimeException("Null passed")
    }
    return "\u001b[" + (30 + ord) + "m" + direction
}

fun temperatureMapper(tile: Tile): String {
    val colour = when {
        tile.temperature < -20 -> "\u001b[44m"
        tile.temperature < -10 -> "\u001b[104m"
        tile.temperature < 0 -> "\u001b[46m"
        tile.temperature < 10 -> "\u001b[47m"
        tile.temperature < 20 -> "\u001b[43m"
        else -> "\u001b[41m"
    }
    return "\u001b[90m" + colour + abs(tile.temperature % 10)
}

fun hotnessMapper(mapper: (Tile) -> Int, step: Int, tile: Tile): String {
    val result = mapper(tile)
    val colour = when {
        result < step -> "\u001b[44m"
        result < 2 * step -> "\u001b[104m"
        result < 3 * step -> "\u001b[46m"
        result < 4 * step -> "\u001b[47m"
        result < 5 * step -> "\u001b[43m"
        else -> "\u001b[41m"
    }
    return "\u001b[90m" + colour + result % 10
}

fun windMap(tile: Tile): String {
    var direction: String
    val level = tile.wind.affectedTiles
            .map { ceil(it.second).toInt() }
            .fold(Int.MIN_VALUE) {x, y -> max(x, y)}
    direction = when {
        level > 4 -> "\u001b[41m"
        level > 3 -> "\u001b[43m"
        level > 2 -> "\u001b[47m"
        level > 1 -> "\u001b[46m"
        else -> "\u001b[44m"
    }
    if (tile.wind.affectedTiles.size >= 1) {
        val affected = tile.wind.affectedTiles.sortedBy { -it.second }[0].first
        direction += when {
            affected.x - tile.x == 1 && affected.y - tile.y == 1 -> "J"
            affected.x - tile.x == 1 && affected.y - tile.y == -1 -> "L"
            affected.x - tile.x == -1 && affected.y - tile.y == 1 -> "⏋"
            affected.x - tile.x == -1 && affected.y - tile.y == -1 -> "Г"
            affected.x - tile.x == 1 -> "V"
            affected.x - tile.x == -1 -> "^"
            affected.y - tile.y == 1 -> ">"
            affected.y - tile.y == -1 -> "<"
            else -> " "//TODO look into it
        }
    } else
        direction = " "
    return direction
}