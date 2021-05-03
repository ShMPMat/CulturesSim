package shmp.visualizer

import shmp.simulation.World
import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.group.GroupConglomerate
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.event.Event
import shmp.simulation.space.WorldMap
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.ResourceType
import shmp.simulation.space.resource.container.ResourcePack
import shmp.simulation.space.resource.free
import shmp.simulation.space.tile.Tile
import shmp.utils.chompToSize
import shmp.visualizer.printinfo.ConglomeratePrintInfo
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

fun printProduced(group: GroupConglomerate) = group.subgroups
        .flatMap { it.cultureCenter.aspectCenter.aspectPool.converseWrappers }
        .flatMap { it.producedResources }
        .distinctBy { it.fullName }
        .sortedBy { it.fullName }
        .joinToString { it.fullName }

fun printApplicableResources(aspect: Aspect, resources: Collection<Resource>) = resources
        .filter { it.genome.conversionCore.actionConversion.containsKey(aspect.core.resourceAction) }
        .joinToString { it.fullName }

fun outputResource(resource: Resource): String = resource.toString() + "\n" +
        resource.genome.conversionCore.actionConversion.entries.joinToString("\n") { (a, v) ->
            a.name + ": " + v.joinToString { it.first?.fullName ?: "LEGEND" }
        } + "\n\n" +
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

fun outputGroup(group: Group) = "$group"

fun printedConglomerates(conglomerates: List<GroupConglomerate>, info: ConglomeratePrintInfo): String {
    val conglomeratesToPrint = 5
    val main = StringBuilder()
    for (group in conglomerates.takeLast(conglomeratesToPrint)) {
        val stringBuilder = StringBuilder()
        if (group.state === GroupConglomerate.State.Dead)
            continue

        stringBuilder.append(info.getConglomerateSymbol(group)).append(" ").append(group.name).append(" \u001b[31m")
        val aspects = group.aspects.sortedByDescending { it.usefulness }
        for (aspect in aspects) {
            if (aspect.usefulness <= 0)
                break
            stringBuilder.append("(").append(aspect.name).append(" ").append(aspect.usefulness)
                    .append(") ")
        }
        stringBuilder.append(" \u001b[32m\n")

        group.cultureAspects.forEach {
            stringBuilder.append("($it)")
        }

        stringBuilder.append(" \u001b[33m\n")
        group.memes.sortedByDescending { it.importance }.take(10).forEach {
            stringBuilder.append("($it ${it.importance})")
        }
        stringBuilder.append(" \u001b[39m\n")

        val hasGrown = group.population > info.populations[group] ?: 0
        stringBuilder.append(if (hasGrown) "\u001b[32m" else "\u001b[31m")
                .append("population=${group.population}")
                .append(if (hasGrown) "↑" else "↓")
                .append("\u001b[39m\n\n")
        info.populations[group] = group.population
        main.append(chompToSize(stringBuilder.toString(), 220))
    }
    return main.toString()
}

fun printConglomerateRelations(conglomerate1: GroupConglomerate, conglomerate2: GroupConglomerate) = """
        |${printConglomerateRelation(conglomerate1, conglomerate2)}
        |
        |
        |${printConglomerateRelation(conglomerate2, conglomerate1)}
        """.trimMargin()

fun printConglomerateRelation(conglomerate1: GroupConglomerate, conglomerate2: GroupConglomerate) =
        conglomerate1.subgroups.joinToString("\n\n") { g ->
            """${g.name} - ${g.processCenter.type}:
                |average - ${g.relationCenter.getAvgConglomerateRelation(conglomerate2)}
                |min -     ${g.relationCenter.getMinConglomerateRelation(conglomerate2)}
                |max -     ${g.relationCenter.getMaxConglomerateRelation(conglomerate2)}
                |In particular:
                |${g.relationCenter.getConglomerateGroups(conglomerate2).joinToString("\n")}
                |""".trimMargin()
        }

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

fun printGroupStatistics(world: World): String {
    val conglomerates = world.groups.filter { it.state == GroupConglomerate.State.Live }

    return """CONGLOMERATES:
             |
             |Largest population: ${conglomerates.maxByOrNull { it.population }?.let { g -> "${g.name} - ${g.population}" }}
             |Largest territory:  ${conglomerates.maxByOrNull { it.territory.size }?.let { g -> "${g.name} - ${g.territory.size}" }}
             |Most groups:        ${conglomerates.maxByOrNull { it.subgroups.size }?.let { g -> "${g.name} - ${g.subgroups.size}" }}
             |
             |${printGroupCharacterStatistics(conglomerates)}
             |
             |GROUPS:
             |
             |Richest common sense:
             |${
        conglomerates.flatMap { it.subgroups }
                .maxByOrNull { it.cultureCenter.cultureAspectCenter.reasonField.commonReasonings.reasonings.size }
                ?.let { g -> "${g.name} - ${g.cultureCenter.cultureAspectCenter.reasonField.commonReasonings}" }
    }
             |
             |Most additional conversions:
             |${
        conglomerates.flatMap { it.subgroups }
                .maxByOrNull { it.cultureCenter.cultureAspectCenter.reasonField.specialConversions.size }
                ?.let { g -> 
                    "${g.name} - ${
                        g.cultureCenter.cultureAspectCenter.reasonField.specialConversions.joinToString("\n")
                    }" 
                }
    }
             |
             |""".trimMargin()
}

fun printGroupCharacterStatistics(conglomerates: List<GroupConglomerate>) =
        Trait.values().joinToString("\n\n") { trait ->
            val avgTraits = conglomerates
                    .filter { it.subgroups.isNotEmpty() }
                    .map { c -> c to c.subgroups.map { it.cultureCenter.traitCenter.processedValue(trait) }.average() }

            val maxStr = avgTraits.maxByOrNull { it.second }?.let { (g, a) -> "Max $trait:   ${g.name} - $a" } ?: ""
            val minStr = avgTraits.minByOrNull { it.second }?.let { (g, a) -> "Min $trait:   ${g.name} - $a" } ?: ""

            maxStr + "\n" + minStr
        }
