package io.tashtabash.visualizer.text

import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.TectonicPlate
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.ResourceType
import io.tashtabash.sim.space.tile.Tile
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max


const val MARK_COLOR = "\u001b[31m"
const val MARK = MARK_COLOR + "X"
const val NOTHING = ""


fun levelMapper(tile: Tile): String {
    val colour = when {
        tile.secondLevel < 90 -> "\u001b[44m"
        tile.secondLevel < data.defaultWaterLevel -> "\u001b[104m"
        tile.secondLevel < 105 -> "\u001b[46m"
        tile.secondLevel < 110 -> "\u001b[47m"
        tile.secondLevel < 130 -> "\u001b[43m"
        else -> "\u001b[41m"
    }
    return "\u001b[90m" + colour + abs(tile.secondLevel % 10)
}

fun predicateMapper(tile: Tile, predicate: (Tile) -> Boolean) =
        if (predicate(tile)) MARK
        else NOTHING

fun vapourMapper(tile: Tile): String {
    val level = tile.resourcesWithMoved
            .filter { it.simpleName == "Vapour" }
            .map { it.amount }.fold(0, Int::plus)
    val colour = when {
        level == 0 -> "\u001b[44m"
        level < 1000 -> "\u001b[104m"
        level < 10000 -> "\u001b[46m"
        level < 100000 -> "\u001b[47m"
        level < 1000000 -> "\u001b[43m"
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


fun hotnessMapper(step: Int, tile: Tile, mapper: (Tile) -> Double, start: Int = 1): String {
    val result = mapper(tile)
    val colour = when {
        result < start -> NOTHING
        result < start + step -> "\u001b[44m"
        result < start + 2 * step -> "\u001b[104m"
        result < start + 3 * step -> "\u001b[46m"
        result < start + 4 * step -> "\u001b[47m"
        result < start + 5 * step -> "\u001b[43m"
        else -> "\u001b[41m"
    }

    return if (result >= start)
        "\u001b[90m" + colour + abs(((result - start) % step) / (ceil(step.toDouble() / 10).toInt()))
    else NOTHING
}

fun temperatureMapper(tile: Tile) = hotnessMapper(10, tile, Tile::temperature, start = -30)

fun windMapper(tile: Tile): String {
    var direction: String
    val level = tile.wind.affectedTiles
            .map { ceil(it.second).toInt() }
            .fold(Int.MIN_VALUE) { x, y -> max(x, y) }
    direction = when {
        level > 4 -> "\u001b[41m"
        level > 3 -> "\u001b[43m"
        level > 2 -> "\u001b[47m"
        level > 1 -> "\u001b[46m"
        else -> "\u001b[44m"
    }
    if (tile.wind.affectedTiles.size >= 1) {
        val affected = tile.wind.affectedTiles.sortedByDescending { it.second }[0].first
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

fun resourceTypeMapper(type: ResourceType, tile: Tile) =
        if (tile.resourcePack.any { it.genome.type == type }) MARK
        else NOTHING

fun resourceSubstringMapper(substring: String, tile: Tile, symbol: String = MARK) =
        if (tile.resourcesWithMoved.any { it.fullName.contains(substring) and it.isNotEmpty }) symbol
        else NOTHING

fun resourceOwnerMapper(ownerSubstring: String, tile: Tile) =
        if (tile.resourcePack.any { it.ownershipMarker.name.contains(ownerSubstring) }) MARK
        else NOTHING

fun resourceDensityMapper(threshold: Double, tile: Tile) = hotnessMapper(
        (threshold / 5.0).toInt(),
        tile,
        { it.resourceDensity }
)

fun tileTagMapper(tagName: String, tile: Tile) = predicateMapper(tile) { t ->
    t.tagPool.all.any { it.name.contains(tagName) }
}

fun ecosystemTypeMapper(resourceSymbols: Map<Resource, String>, tile: Tile) = when (tile.type) {
    Tile.Type.Water, Tile.Type.Ice, Tile.Type.Woods, Tile.Type.Growth, Tile.Type.Normal -> {
        val actual = tile.resourcePack.getResourcesUnpacked { r ->
            r.genome.type !== ResourceType.Plant && r.isNotEmpty && r.simpleName != "Vapour"
        }
        if ( /*actual.size() > 0*/false) {
            "\u001b[30m" + if (resourceSymbols[actual[0]] == null) "Ё" else resourceSymbols[actual[0]]
        } else {
            " "
        }
    }
    else -> " "
}
