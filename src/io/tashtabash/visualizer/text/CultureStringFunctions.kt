package io.tashtabash.visualizer.text

import io.tashtabash.sim.CulturesWorld
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.group.GroupConglomerate
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.intergroup.Relation
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.utils.chompToSize
import io.tashtabash.visualizer.printinfo.ConglomeratePrintInfo


fun printProduced(group: GroupConglomerate) = group.subgroups
        .flatMap { it.cultureCenter.aspectCenter.aspectPool.converseWrappers }
        .flatMap { it.producedResources }
        .distinctBy { it.fullName }
        .sortedBy { it.fullName }
        .joinToString { it.fullName }

fun printApplicableResources(aspect: Aspect, resources: Collection<Resource>) = resources
        .filter { it.genome.conversionCore.actionConversions.containsKey(aspect.core.resourceAction) }
        .joinToString { it.fullName }

fun outputGroup(group: Group) = "$group"

fun printedConglomerates(conglomerates: List<GroupConglomerate>, info: ConglomeratePrintInfo): String {
    val conglomeratesPrintNumber = 5
    val printedConglomerates = conglomerates.filter { it.state != GroupConglomerate.State.Dead }
        .takeLast(conglomeratesPrintNumber)
    val main = StringBuilder()
    for (group in printedConglomerates) {
        val stringBuilder = StringBuilder()

        stringBuilder.append(info.getConglomerateSymbol(group)).append(" ").append(group.name).append(" \u001b[31m")
        val aspects = group.aspects.sortedByDescending { it.usefulness }.take(10)
        for (aspect in aspects) {
            if (aspect.usefulness <= 0)
                break
            stringBuilder.append("(").append(aspect.name).append(" ").append(aspect.usefulness)
                    .append(") ")
        }
        stringBuilder.append(" \u001b[32m\n")

        group.cultureAspects.take(10).forEach {
            stringBuilder.append("($it)")
        }

        stringBuilder.append(" \u001b[33m\n")
        group.memes.sortedByDescending { it.importance }.take(10).forEach {
            stringBuilder.append("($it ${it.importance})")
        }
        stringBuilder.append(" \u001b[39m\n")
            .append(makeTraitString(group))
            .append(makePopulationString(group, info))

        info.populations[group] = group.population
        main.append(chompToSize(stringBuilder.toString(), 220))
    }
    return main.toString()
}

private fun makePopulationString(conglomerate: GroupConglomerate, info: ConglomeratePrintInfo): String {
    val hasGrown = conglomerate.population > (info.populations[conglomerate] ?: 0)

    return (if (hasGrown) "\u001b[32m" else "\u001b[31m") +
            "\npopulation=${conglomerate.population}" +
            (if (hasGrown) "↑" else "↓") +
                    "\u001b[39m\n\n"
}

private fun makeTraitString(conglomerate: GroupConglomerate) = Trait.entries
    .joinToString { trait ->
        val avg = conglomerate.subgroups
            .map { it.cultureCenter.traitCenter.processedValue(trait) }
            .average()
        "${trait.name}: %+.3f".format(avg)
    }

fun printGroupRelations(group1: Group, group2: Group) = """
        |${printGroupRelation(group1, group2)}
        |${printGroupRelation(group2, group1)}
        """.trimMargin()

fun printGroupRelation(group1: Group, group2: Group) =
    "${group1.name} - ${group1.processCenter.type}: ${group1.relationCenter.getRelation(group2)}"

fun printGroupStatistics(world: CulturesWorld): String {
    val conglomerates = world.conglomerates.filter { it.state == GroupConglomerate.State.Live }
    val clusters = clusterRelations(
        world.conglomerates.flatMap { it.subgroups }
            .flatMap { it.relationCenter.relations },
        0.6
    )

    return """CONGLOMERATES:
             |
             |Largest population:  ${conglomerates.maxByOrNull { it.population }?.let { g -> "${g.name} - ${g.population}" }}
             |Largest territory:   ${conglomerates.maxByOrNull { it.territory.size }?.let { g -> "${g.name} - ${g.territory.size}" }}
             |Most groups:         ${conglomerates.maxByOrNull { it.subgroups.size }?.let { g -> "${g.name} - ${g.subgroups.size}" }}
             |Max free population: ${
        conglomerates.map { it to it.subgroups.sumOf { g -> g.populationCenter.freePopulation } }
                .maxByOrNull { it.second }?.let { (g, n) -> "${g.name} - $n" }
    }
             |
             |${printGroupCharacterStatistics(conglomerates)}
             |
             |GROUPS:
             |
             |Richest common sense:
             |${
        conglomerates.flatMap { it.subgroups }
                .maxByOrNull { it.cultureCenter.cultureAspectCenter.reasonField.commonReasons.reasonings.size }
                ?.let { g -> "${g.name} - ${g.cultureCenter.cultureAspectCenter.reasonField.commonReasons}" }
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
             |Relation clusters
             |${
                 clusters.sortedBy { c -> c.minOf { it.name } }
                     .joinToString(separator = "\n") { c -> c.sortedBy { it.name }.joinToString { it.name } }
             }
             |""".trimMargin()
}

fun printGroupCharacterStatistics(conglomerates: List<GroupConglomerate>) =
        Trait.entries.joinToString("\n\n") { trait ->
            val avgTraits = conglomerates
                    .filter { it.subgroups.isNotEmpty() }
                    .map { c -> c to c.subgroups.map { it.cultureCenter.traitCenter.processedValue(trait) }.average() }

            val maxStr = avgTraits.maxByOrNull { it.second }?.let { (g, a) -> "Max $trait:   ${g.name} - $a" } ?: ""
            val minStr = avgTraits.minByOrNull { it.second }?.let { (g, a) -> "Min $trait:   ${g.name} - $a" } ?: ""

            maxStr + "\n" + minStr
        }

fun clusterRelations(relations: List<Relation>, threshold: Double = 0.75): List<Set<Group>> {
    val allGroups = relations.flatMap { listOf(it.owner, it.other) }.toSet()
    val clusters = allGroups.map { mutableSetOf(it) }.toMutableList()

    // Create a lookup using smallerHash to largerHash to treat relations as undirected
    val scores = mutableMapOf<Pair<Group, Group>, Double>()
    for (rel in relations) {
        val pair = if (rel.owner.hashCode() < rel.other.hashCode())
            rel.owner to rel.other
        else
            rel.other to rel.owner

        // If asymmetrical, take the average positivity
        val current = scores.getOrDefault(pair, 0.0)
        scores[pair] = if (current == 0.0) rel.normalized else (current + rel.normalized) / 2
    }

    val sortedRelations = scores.entries
        .filter { it.value >= threshold }
        .sortedByDescending { it.value }

    // Merge
    for (entry in sortedRelations) {
        val (g1, g2) = entry.key
        val cluster1 = clusters.firstOrNull { g1 in it }
        val cluster2 = clusters.firstOrNull { g2 in it }

        if (cluster1 != null && cluster2 != null && cluster1 != cluster2) {
            cluster1 += cluster2
            clusters -= cluster2
        }
    }

    return clusters
}

