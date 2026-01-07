package io.tashtabash.visualizer.text

import io.tashtabash.sim.CulturesWorld
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.group.GroupConglomerate
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
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

fun printGroupStatistics(world: CulturesWorld): String {
    val conglomerates = world.conglomerates.filter { it.state == GroupConglomerate.State.Live }

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
