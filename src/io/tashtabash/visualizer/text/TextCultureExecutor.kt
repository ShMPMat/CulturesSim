package io.tashtabash.visualizer.text

import io.tashtabash.utils.chompToSize
import io.tashtabash.visualizer.*
import io.tashtabash.visualizer.command.Command
import io.tashtabash.visualizer.command.CommandExecutor
import io.tashtabash.visualizer.command.CultureCommand.*
import io.tashtabash.visualizer.command.ExecutionResult


object TextCultureExecutor : CommandExecutor<TextCultureVisualizer> {
    override fun tryRun(line: String, command: Command, visualizer: TextCultureVisualizer): ExecutionResult {
        val splitCommand = line.split(" ")

        visualizer.apply {
            val world = controller.world

            when (command) {
                Conglomerate -> {
                    val conglomerate = world.groups
                            .firstOrNull { it.name == line }
                    val group = world.groups
                            .flatMap { it.subgroups }
                            .firstOrNull { it.name == line }
                    when {
                        conglomerate != null -> printGroupConglomerate(conglomerate)
                        group != null -> printGroup(group)
                        else -> {
                            println("No such Group or Conglomerate exist")

                            return ExecutionResult.Invalid
                        }
                    }
                }
                ConglomerateTileReach -> {
                    val conglomerate = getConglomerate(splitCommand[0])
                            ?: return ExecutionResult.Invalid
                    printMap { conglomerateReachMapper(conglomerate, it) }
                }
                ConglomerateProduced -> {
                    val conglomerate = getConglomerate(splitCommand[0])
                            ?: return ExecutionResult.Invalid
                    println(chompToSize(printProduced(conglomerate), 150))
                }
                GroupRelations -> {
                    val c1 = getConglomerate(splitCommand[0])
                    val c2 = getConglomerate(splitCommand[1])
                    if (c1 == null || c2 == null)
                        return ExecutionResult.Invalid

                    println(printConglomerateRelations(c1, c2))
                }
                ConglomeratePotentials -> {
                    val conglomerate = getConglomerate(splitCommand[0])
                            ?: return ExecutionResult.Invalid
                    printMap { t ->
                        hotnessMapper(
                                splitCommand[2].toInt(),
                                t,
                                { conglomerate.subgroups[0].territoryCenter.tilePotentialMapper(it) },
                                splitCommand[2].toInt()
                        )
                    }
                }
                MeaningfulResources -> printMap { meaningfulResourcesMapper(it) }
                ArtificialResources -> printMap { artificialResourcesMapper(it) }
                GroupStatistics -> println(printGroupStatistics(world))
                Aspects -> {
                    printMap { aspectMapper(splitCommand[1], it) }
                    world.aspectPool.all.filter { it.name.contains(splitCommand[1]) }
                            .forEach {
                                println(it.name)
                                println(printApplicableResources(it, world.resourcePool.all))
                                println()
                            }
                    println("\nActual instances:\n")
                    world.groups.flatMap { it.aspects }
                            .filter { it.name.contains(splitCommand[1]) }
                            .distinct()
                            .sortedBy { it.name }
                            .forEach { aspect ->
                                println(aspect.name)
                                println(printApplicableResources(aspect, world.resourcePool.all))
                                println()
                            }
                }
                CultureAspects -> {
                    printMap { cultureAspectMapper(splitCommand[1], it) }
                    world.groups.flatMap { it.subgroups }
                            .flatMap { it.cultureCenter.cultureAspectCenter.aspectPool.all }
                            .filter { it.toString().contains(splitCommand[1]) }
                            .distinct()
                            .sortedBy { it.toString() }
                            .forEach { println(it) }
                }
                Strata -> printMap { strataMapper(splitCommand[1], it) }
                AddAspect -> addGroupConglomerateAspect(
                        getConglomerate(splitCommand[0]),
                        splitCommand[1],
                        world.aspectPool
                )
                AddWant -> addGroupConglomerateWant(
                        getConglomerate(splitCommand[1]),
                        splitCommand[2],
                        world.resourcePool
                )
                else -> return ExecutionResult.NotFound
            }
        }
        return ExecutionResult.NotFound
    }

    private fun TextCultureVisualizer.getConglomerate(name: String) = controller.world.groups
            .firstOrNull { it.name == name }
            .apply {
                if (this == null)
                    println("Conglomerate '$name' not found")
            }
}
