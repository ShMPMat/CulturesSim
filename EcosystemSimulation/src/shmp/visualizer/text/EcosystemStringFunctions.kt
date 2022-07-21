package shmp.visualizer.text

import shmp.simulation.World
import shmp.simulation.event.Event
import shmp.simulation.space.WorldMap
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.ResourceType
import shmp.simulation.space.resource.Taker
import shmp.simulation.space.resource.container.ResourcePack
import shmp.simulation.space.resource.dependency.ConsumeDependency
import shmp.simulation.space.resource.dependency.NeedDependency
import shmp.simulation.space.resource.free
import shmp.simulation.space.tile.Tile
import shmp.utils.addToRight
import shmp.utils.chompToLines
import shmp.utils.chompToSize
import java.util.regex.PatternSyntaxException


fun basicResourcesCounter(world: World): String {
    val resourceAmounts = world.resourcePool.all
            .filter { it.genome.type in listOf(ResourceType.Animal, ResourceType.Plant) }
            .map { it to ResourceCount() }
            .toMap()
    world.map.tiles.forEach { t ->
        t.resourcePack.resources.forEach {
            if (resourceAmounts.containsKey(it))
                resourceAmounts.getValue(it).add(it)
        }
    }
    val lines =  resourceAmounts.entries.joinToString("\n") {
        val colourMark = when (it.value.tilesAmount) {
            0 -> "\u001b[31m"
            in 1..99 -> "\u001b[33m"
            else -> "\u001b[30m"
        }
        "$colourMark${it.key.fullName}: tiles - ${it.value.tilesAmount}, amount - ${it.value.amount}"
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

data class ResourceCount(var amount: Int = 0, var tilesAmount: Int = 0) {
    fun add(resource: Resource) {
        amount += resource.amount
        tilesAmount++
    }
}

fun outputResource(resource: Resource): String {
    val actionConversions = resource.genome.conversionCore.actionConversion.entries
            .joinToString("\n") { (a, v) ->
                a.name + ": " + v.joinToString { it.fullName + ":" + it.amount }
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

fun printRegexEvents(events: List<Event>, amount: Int, regexString: String) = printEvents(events, amount) {
    try {
        regexString.toRegex().containsMatchIn(it.description)
    } catch (e: PatternSyntaxException) {
        false
    }
}
