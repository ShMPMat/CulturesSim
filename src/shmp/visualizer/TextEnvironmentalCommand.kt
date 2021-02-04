package shmp.visualizer

import shmp.simulation.culture.group.GroupConglomerate
import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.resource.ResourceType
import shmp.utils.chompToSize
import shmp.visualizer.command.*
import shmp.visualizer.command.EnvironmentCommand.*
import java.util.*


fun Pair<String, Command>.runCommand(visualizer: TextVisualizer) {
    val (line, command) = this
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
                val group = getConglomerate(splitCommand[0]) ?: return
                printMap { groupReachMapper(group.subgroups[0], it) }
            }
            GroupProduced -> {
                val group = getConglomerate(splitCommand[0]) ?: return
                println(chompToSize(printProduced(group), 150))
            }
            GroupRelations -> {
                val c1 = getConglomerate(splitCommand[0])
                val c2 = getConglomerate(splitCommand[1])
                if (c1 == null || c2 == null) {
                    println("No such Conglomerates exist")
                    return
                }
                println(printConglomerateRelations(c1, c2))
            }
            Tile -> map[splitCommand[0].toInt(), splitCommand[1].toInt() + mapPrintInfo.cut]?.let {
                printTile(
                        it
                )
            } ?: print("No such Tile")
            Plates -> printMap { platesMapper(map.tectonicPlates, it) }
            Temperature -> printMap { temperatureMapper(it) }
            GroupPotentials -> {
                val group = getConglomerate(splitCommand[0]) ?: return
                printMap { t ->
                    hotnessMapper(
                            splitCommand[2].toInt(),
                            t,
                            { group.subgroups[0].territoryCenter.tilePotentialMapper(it) },
                            splitCommand[2].toInt()
                    )
                }
            }
            Wind -> printMap { windMapper(it) }
            TerrainLevel -> printMap { levelMapper(it) }
            Vapour -> printMap { vapourMapper(it) }
            MeaningfulResources -> printMap { meaningfulResourcesMapper(it) }
            ArtificialResources -> printMap { artificialResourcesMapper(it) }
            TileTag -> printMap { tileTagMapper(splitCommand[1], it) }
            Resource -> try {
                val resource = world.resourcePool.getBaseName(line.substring(2))
                printResource(resource)
            } catch (e: NoSuchElementException) {
                resourceSymbols.entries
                        .filter { it.value == line.substring(2) }
                        .map { it.key }
                        .firstOrNull()
                        ?.let { printResource(it) }
            }
            ResourceSubstring -> {
                printMap { resourceSubstringMapper(splitCommand[1], it) }
                println(briefPrintResourcesWithSubstring(map, splitCommand[1]))
            }
            ResourceSubstringOnTile -> println(printResourcesOnTile(
                    map[splitCommand[0].toInt(),
                            splitCommand[1].toInt() + mapPrintInfo.cut]!!,
                    splitCommand[3]
            ))
            ResourceType -> if (ResourceType.values().toList().any { it.toString() == splitCommand[1] }) {
                val type = ResourceType.valueOf(splitCommand[1])
                printMap { resourceTypeMapper(type, it) }
            } else
                println("Unknown type - " + splitCommand.get(1))
            ResourceOwner -> printMap { resourceOwnerMapper(splitCommand[1], it) }
            AllResources -> println(resourcesCounter(world))
            ResourceDensity -> printMap { resourceDensityMapper(data.tileResourceCapacity, it) }
            Events -> {
                var amount = 100
                var drop = splitCommand[0].length + 1
                if (splitCommand[0][0] != 'e') {
                    amount = splitCommand[0].toInt()
                    drop += splitCommand[1].length + 1
                }
                val regexp = line.substring(drop)
                println(printRegexEvents(
                        controller.interactionModel.eventLog.lastEvents,
                        amount,
                        regexp
                ))
            }
            Aspects -> {
                printMap { aspectMapper(splitCommand[1], it) }
                world.aspectPool.get(splitCommand[1])?.let { aspect ->
                    println(printApplicableResources(aspect, world.resourcePool.all))
                }
            }
            Strata -> {
                printMap { strataMapper(splitCommand[1], it) }
            }
            ShowMap -> printMap { "" }
            Exit -> return
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
            AddResource -> addResourceOnTile(
                    map[splitCommand[0].toInt(), splitCommand[1].toInt()],
                    splitCommand[2],
                    world.resourcePool
            )
            GeologicalTurn -> {
                controller.geologicTurn()
                print()
            }
            Turner -> line.toIntOrNull()?.let {
                launchTurner(it)
            } ?: println("Wrong number format for amount of turns")
            Turn -> {
                controller.turn()
                print()
            }
            Pass -> println("No action taken")
            else -> {
                (line to CommandManager.defaultCommand).runCommand(this)
            }
        }
    }
}

private fun TextVisualizer.getConglomerate(string: String): GroupConglomerate? {
    val index = string.substring(1).toInt()
    val world = controller.world

    return if (index < world.groups.size)
        world.groups[index]
    else null
}
