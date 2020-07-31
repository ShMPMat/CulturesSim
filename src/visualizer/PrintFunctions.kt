package visualizer

import extra.chompToSize
import simulation.World
import simulation.culture.aspect.Aspect
import simulation.culture.group.GroupConglomerate
import simulation.culture.group.centers.Group
import simulation.event.Event
import simulation.space.WorldMap
import simulation.space.resource.Resource
import simulation.space.resource.ResourceType
import simulation.space.tile.Tile
import visualizer.printinfo.ConglomeratePrintInfo
import java.util.regex.PatternSyntaxException


fun resourcesCounter(world: World): String {
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

fun printResource(resource: Resource): String = resource.toString() + "\n" +
        resource.genome.conversionCore.actionConversion.entries.joinToString("\n") { (a, v) ->
            a.name + ": " + v.joinToString { it.first?.fullName ?: "LEGEND" }
        } + "\n\n" +
        resource.genome.parts.joinToString("\n") { p ->
            printResource(p).lines().joinToString("\n") { "--$it" }
        }

fun printResources(resources: List<Resource>) = resources
        .joinToString("\n\n\n\n") { printResource(it) }

fun printResourcesOnTile(tile: Tile, substring: String) =
        printResources(tile.resourcesWithMoved.filter { it.fullName.contains(substring) })

fun briefPrintResourcesWithSubstring(map: WorldMap, substring: String) = map.tiles
        .flatMap { t -> t.resourcesWithMoved.map { r -> r to t } }
        .filter { it.first.fullName.contains(substring) }
        .joinToString("\n") { (r, t) -> "${t.x} ${t.y}: ${r.fullName} - ${r.amount}, ${r.ownershipMarker}" }

fun printGroup(group: Group) = "$group"

fun printedConglomerates(conglomerates: List<GroupConglomerate>, info: ConglomeratePrintInfo): String {
    val main = StringBuilder()
    for (group in conglomerates.takeLast(20)) {
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
