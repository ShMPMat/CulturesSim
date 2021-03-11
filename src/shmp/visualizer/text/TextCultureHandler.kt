package shmp.visualizer.text

import shmp.simulation.culture.group.GroupConglomerate
import shmp.utils.chompToSize
import shmp.visualizer.*
import shmp.visualizer.command.Command
import shmp.visualizer.command.CommandHandler
import shmp.visualizer.command.CultureCommand.*


object TextCultureHandler: CommandHandler<TextEcosystemVisualizer> {
    override fun tryRun(line: String, command: Command, visualizer: TextEcosystemVisualizer): Boolean {
        val splitCommand = line.split(" ")

        visualizer.apply {
            val world = controller.world
            val map = world.map
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
                        else -> println("No such Group or Conglomerate exist")
                    }
                }
                GroupTileReach -> {
                    val group = getConglomerate(splitCommand[0]) ?: return true
                    printMap { groupReachMapper(group.subgroups[0], it) }
                }
                GroupProduced -> {
                    val group = getConglomerate(splitCommand[0]) ?: return true
                    println(chompToSize(printProduced(group), 150))
                }
                GroupRelations -> {
                    val c1 = getConglomerate(splitCommand[0])
                    val c2 = getConglomerate(splitCommand[1])
                    if (c1 == null || c2 == null) {
                        println("No such Conglomerates exist")
                        return true
                    }
                    println(printConglomerateRelations(c1, c2))
                }
                GroupPotentials -> {
                    val group = getConglomerate(splitCommand[0]) ?: return true
                    printMap { t ->

                        hotnessMapper(
                                splitCommand[2].toInt(),
                                t,
                                { group.subgroups[0].territoryCenter.tilePotentialMapper(it) },
                                splitCommand[2].toInt()
                        )
                    }
                }
                MeaningfulResources -> printMap { meaningfulResourcesMapper(it) }
                ArtificialResources -> printMap { artificialResourcesMapper(it) }
                GroupStatistics -> println(printGroupStatistics(world))
                Aspects -> {
                    printMap { aspectMapper(splitCommand[1], it) }
                    world.aspectPool.get(splitCommand[1])?.let { aspect ->
                        println(printApplicableResources(aspect, world.resourcePool.all))
                    }
                }
                Strata -> {
                    printMap { strataMapper(splitCommand[1], it) }
                }
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
                else -> return false
            }
        }
        return true
    }

    private fun TextEcosystemVisualizer.getConglomerate(string: String): GroupConglomerate? {
        val index = string.substring(1).toInt()
        val world = controller.world

        return if (index < world.groups.size)
            world.groups[index]
        else null
    }
}