package io.tashtabash.visualizer.text

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.tile.Tile


fun TextEcosystemVisualizer<*>.printTile(tile: Tile) {
    printMap { if (it == tile) "\u001b[31m\u001b[41mX" else "" }
    println(tile)
}

fun TextEcosystemVisualizer<*>.printTiles(tiles: Collection<Tile>) {
    if (tiles.isEmpty()) {
        print("No such Tiles")

        return
    }

    val types = tiles.map { it.type }.distinct()

    val tempMin = tiles.minOfOrNull { it.temperature }
    val tempMax = tiles.maxOfOrNull { it.temperature }

    val levelMin = tiles.minOfOrNull { it.level }
    val levelMax = tiles.maxOfOrNull { it.level }

    val tagString = tiles.flatMap { it.tagPool.all }
            .distinct()
            .joinToString("; ")

    val resourceString = tiles.flatMap { it.resourcePack.resources }
            .groupBy { it }
            .map { (k, rs) -> k.copy(rs.sumBy { it.amount }) }
            .map { it.toString() }
            .sorted()
            .joinToString("\n", "\n", "\n")

    printMap { if (it in tiles) "\u001b[31m\u001b[41mX" else "" }
    println(
            "type=$types, temperature=$tempMin-$tempMax, level=$levelMin-$levelMax\n" +
            "Tags: $tagString\n\nResources:$resourceString"
    )
}

fun TextEcosystemVisualizer<*>.printResource(resource: Resource) {
    printMap { tile: Tile ->
        if (tile.resourcesWithMoved.any { it.baseName == resource.baseName && it.isNotEmpty })
            "\u001b[30m\u001b[41m" + tile.resourcePack.getAmount(resource) % 10
        else ""
    }
    println(outputResource(resource))
    println(outputFoodWeb(resource, controller.world))
}