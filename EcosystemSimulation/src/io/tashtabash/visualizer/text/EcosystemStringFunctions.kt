package io.tashtabash.visualizer.text

import io.tashtabash.simulation.World
import io.tashtabash.simulation.event.Event
import io.tashtabash.simulation.space.WorldMap
import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.ResourceType
import io.tashtabash.simulation.space.resource.container.ResourcePack
import io.tashtabash.simulation.space.resource.dependency.ConsumeDependency
import io.tashtabash.simulation.space.resource.dependency.NeedDependency
import io.tashtabash.simulation.space.resource.free
import io.tashtabash.simulation.space.tile.Tile
import io.tashtabash.utils.chompToLines


fun aliveResourcesCounter(world: World): String {
    val resourceAmounts = world.resourcePool.all
            .filter { it.genome.type in listOf(ResourceType.Animal, ResourceType.Plant) }
            .associate { it.baseName to ResourceCounter() }
    world.map.tiles.forEach { t ->
        t.resourcePack.resources.forEach {
            resourceAmounts[it.baseName]?.add(it)
        }
    }

    val lines =  resourceAmounts.entries.joinToString("\n") { (resourceName, counter) ->
        val colourMark = when (counter.tilesAmount) {
            0 -> "\u001b[31m"
            in 1..99 -> "\u001b[33m"
            else -> "\u001b[30m"
        }
        "$colourMark$resourceName: tiles - ${counter.tilesAmount}, amount - ${counter.amount}"
    }

    return chompToLines(lines, 30)
            .append("\u001b[30m")
            .toString()
}

fun allResourcesCounter(world: World, shouldFree: Boolean): String {
    val allResources = world.map.tiles
            .flatMap { t -> t.resourcePack.resources.map { it.exactCopy() } }
            .map { if (shouldFree) it.free() else it }

    val pack = ResourcePack(allResources)

    return pack.resources
            .sortedBy { it.amount }
            .joinToString("\n") { "${it.fullName}, ${it.ownershipMarker} ${it.amount};" }
}

data class ResourceCounter(var amount: Int = 0, var tilesAmount: Int = 0) {
    fun add(resource: Resource) {
        amount += resource.amount
        tilesAmount++
    }
}

fun outputResource(resource: Resource): String {
    val actionConversions = resource.genome.conversionCore.actionConversion.entries
            .joinToString("\n") { (a, v) ->
                val needClause = if (a.dependencies.isNotEmpty())
                    a.dependencies.joinToString(",", " needs ", " ")
                else ""

                a.name + needClause + ": " +
                        v.joinToString { it.fullName + ":" + it.amount }
            }

    val parts = resource.genome.parts.joinToString("\n") { p ->
        outputResource(p).lines().joinToString("\n") { "--$it" }
    }

    return "$resource\n$actionConversions\n\nParts:\n$parts"
}

fun outputFoodWeb(resource: Resource, world: World): String {
    val consumers = world.resourcePool.all
            .asSequence()
            .filter {
                it.genome.dependencies.filterIsInstance<ConsumeDependency>()
                    .any { r -> r.lastConsumed(it.baseName).contains(resource.baseName) }
            }
            .map { it.baseName }
            .distinct()
            .joinToString("\n")

    val consumed = resource.genome.dependencies
            .filterIsInstance<ConsumeDependency>()
            .flatMap { it.lastConsumed(resource.baseName) }
            .joinToString("\n")

    val needed = resource.genome.dependencies
            .filterIsInstance<NeedDependency>()
            .flatMap { it.lastConsumed(resource.baseName) }
            .joinToString("\n")

    return "Consumers:\n$consumers\n\nConsumed:\n$consumed\n\nNeeded:\n$needed"
}

fun printResources(resources: List<Resource>) = resources
        .joinToString("\n\n\n\n") { outputResource(it) }

fun printResourcesOnTile(tile: Tile, substring: String) =
        printResources(tile.resourcesWithMoved.filter { it.fullName.contains(substring) })

fun briefPrintResourcesWithSubstring(map: WorldMap, substring: String) = map.tiles
        .flatMap { t -> t.resourcesWithMoved.map { r -> r to t } }
        .filter { it.first.fullName.contains(substring) }
        .joinToString("\n") { (r, t) -> "${t.posStr}: ${r.fullName} - ${r.amount}, ${r.ownershipMarker}" }

fun printEvents(events: List<Event>, amount: Int, predicate: (Event) -> Boolean) = events
        .filter { predicate(it) }
        .takeLast(amount)
        .joinToString("\n")

fun printRegexEvents(events: List<Event>, amount: Int, regex: Regex) = printEvents(events, amount) {
        regex.containsMatchIn(it.description)
}