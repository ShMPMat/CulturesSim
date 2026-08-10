package io.tashtabash.visualizer.text

import io.tashtabash.sim.World
import io.tashtabash.sim.event.Event
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.ResourceType
import io.tashtabash.sim.space.resource.container.ResourcePack
import io.tashtabash.sim.space.resource.dependency.ConsumeDependency
import io.tashtabash.sim.space.resource.dependency.LabelerDependency
import io.tashtabash.sim.space.resource.dependency.NeedDependency
import io.tashtabash.sim.space.resource.free
import io.tashtabash.sim.space.tile.Tile
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

    return chompToLines(lines, 35)
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

fun outputResourceCharacteristics(resource: Resource): String {
    val actionConversions = resource.genome.conversionCore.allActionConversions.entries
            .joinToString("\n") { (a, v) ->
                val needClause = if (a.dependencies.isNotEmpty())
                    a.dependencies.joinToString(",", " needs ", " ")
                else ""

                a.technicalName + needClause + ": " +
                        v.joinToString { it.fullName + ":" + it.amount }
            }
    val dependencies = resource.genome.dependencies.joinToString("\n")
    val parts = resource.genome.parts.joinToString("\n") { p ->
        outputResourceCharacteristics(p).lines().joinToString("\n") { "--$it" }
    }

    return "$resource\n\n$dependencies\n\n$actionConversions\n\nParts:\n$parts"
}

fun getAmount(resource: Resource, world: World): Int =
    world.map.tiles.flatMap { it.resourcesWithMoved }
        .filter { it.baseName == resource.baseName && it.isNotEmpty }
        .sumOf { it.amount }

fun outputAmount(resource: Resource, world: World) =
    "Amount on map: %,d".format(getAmount(resource, world))

fun outputFoodWeb(resource: Resource, world: World): String {
    fun matchesTarget(fullName: String) =
        fullName == resource.baseName || fullName.startsWith("${resource.baseName}_")
    val resourceAmount = getAmount(resource, world)
        .toDouble()

    var totalConsumedByConsumers = .0
    val consumerLines = world.resourcePool.all.mapNotNull { consumer ->
        val amount = consumer.genome.dependencies
            .filterIsInstance<ConsumeDependency>()
            .sumOf { dep ->
                dep.lastConsumed(consumer.baseName).entries
                    .filter { (fullName, _) -> matchesTarget(fullName) }
                    .sumOf { it.value }
            }
            .toDouble()
        if (amount <= 0) return@mapNotNull null
        totalConsumedByConsumers += amount
        val percent = if (resourceAmount > 0) amount * 100.0 / resourceAmount else 0.0
        "${consumer.baseName}: ${formatStatAmount(amount)} / ${formatStatAmount(resourceAmount)} (%.1f%%)"
            .format(percent)
    }
    val consumerTotalPercent =
        if (resourceAmount > 0)
            totalConsumedByConsumers * 100.0 / resourceAmount
        else .0
    val consumers = (consumerLines + "Total: ${formatStatAmount(totalConsumedByConsumers)} / ${formatStatAmount(resourceAmount)} (%.1f%%)"
        .format(consumerTotalPercent)).joinToString("\n")

    val consumed = formatConsumptionStats(
        resource.genome.dependencies.filterIsInstance<ConsumeDependency>(),
        resourceAmount
    ) { it.lastConsumed(resource.baseName) }

    val needed = formatConsumptionStats(
        resource.genome.dependencies.filterIsInstance<NeedDependency>(),
        resourceAmount
    ) { it.lastConsumed(resource.baseName) }

    return "Consumers:\n$consumers\n\nConsumed:\n$consumed\n\nNeeded:\n$needed"
}

private fun <E: LabelerDependency> formatConsumptionStats(
    dependencies: Iterable<E>,
    resourceNumber: Double,
    lastConsumedFor: (E) -> Map<String, Int>
): String {
    var totalAmount = .0
    var totalRequired = .0
    val lines = dependencies.flatMap { dep ->
        val required = dep.amount * resourceNumber
        totalRequired += required
        val amounts = lastConsumedFor(dep)
        if (amounts.isEmpty())
            listOf("${dep.labeler}: 0 / ${formatStatAmount(required)} (0.0%)")
        else
            amounts.entries.map { (name, amount) ->
                val amountValue = amount.toDouble()
                totalAmount += amountValue
                val percent =
                    if (required > 0)
                        amountValue * 100.0 / required
                    else .0
                "$name: ${formatStatAmount(amountValue)} / ${formatStatAmount(required)} (%.1f%%)".format(percent)
            }
    }
    val totalPercent =
        if (totalRequired > 0)
            totalAmount * 100.0 / totalRequired
        else 100.0
    val totalLine = "Total: ${formatStatAmount(totalAmount)} / ${formatStatAmount(totalRequired)} (%.1f%%)".format(totalPercent)

    return (lines.sorted() + totalLine).joinToString("\n")
}

private fun formatStatAmount(value: Double): String = when {
    value >= 1_000_000_000 -> "%.2fG".format(value / 1_000_000_000)
    value >= 1_000_000 -> "%.2fM".format(value / 1_000_000)
    value >= 10_000 -> "%.2fK".format(value / 1_000)
    value >= 100 -> "%.0f".format(value)
    else -> "%.2f".format(value)
}

fun printResources(resources: List<Resource>) = resources
        .joinToString("\n\n\n\n") { outputResourceCharacteristics(it) }

fun printResourcesOnTile(tile: Tile, substring: String) =
        printResources(tile.resourcesWithMoved.filter { it.fullName.contains(substring) })

fun briefPrintResourcesWithSubstring(map: WorldMap, substring: String) = map.tiles
        .flatMap { t -> t.resourcesWithMoved.map { r -> r to t } }
        .filter { it.first.fullName.contains(substring) }
        .joinToString("\n") { (r, t) -> "${t.posStr}: ${r.fullName} - ${r.amount}, ${r.ownershipMarker}" }

fun briefPrintResourcesWithBaseName(map: WorldMap, baseName: String) = map.tiles
        .flatMap { t -> t.resourcesWithMoved.map { r -> r to t } }
        .filter { it.first.baseName == baseName }
        .joinToString("\n") { (r, t) -> "${t.posStr}: ${r.fullName} - ${r.amount}, ${r.ownershipMarker}" }

fun printEvents(events: List<Event>, amount: Int, predicate: (Event) -> Boolean): String {
    val allEvents = events.filter { predicate(it) }
    val eventLines = allEvents.takeLast(amount)
        .joinToString("\n")

    return "${allEvents.size}\n$eventLines"
}

fun printRegexEvents(events: List<Event>, amount: Int, regex: Regex) = printEvents(events, amount) {
        regex.containsMatchIn(it.toString())
}
