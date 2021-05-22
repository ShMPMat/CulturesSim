package shmp.visualizer.text

import shmp.simulation.World
import shmp.simulation.event.Event
import shmp.simulation.space.WorldMap
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.ResourceType
import shmp.simulation.space.resource.container.ResourcePack
import shmp.simulation.space.resource.free
import shmp.simulation.space.tile.Tile
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
    return resourceAmounts.entries.joinToString("\n", postfix = "\u001b[30m")
    {
        (if (it.value.amount == 0) "\u001b[31m" else "\u001b[30m") +
                "${it.key.fullName}: tiles - ${it.value.tilesAmount}, amount - ${it.value.amount}"
    }
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

fun outputResource(resource: Resource): String = resource.toString() + "\n" +
        resource.genome.conversionCore.actionConversion.entries.joinToString("\n") { (a, v) ->
            a.name + ": " + v.joinToString { (it.first?.fullName ?: "LEGEND") + ":" + it.second }
        } + "\n\nParts:\n" +
        resource.genome.parts.joinToString("\n") { p ->
            outputResource(p).lines().joinToString("\n") { "--$it" }
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
