package visualizer

import simulation.space.TectonicPlate
import simulation.space.tile.Tile
import java.util.*
import kotlin.math.abs

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

fun temperatureMapper(tile: Tile): String? {
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