package shmp.visualizer.text

import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.resource.ResourceType
import shmp.simulation.space.resource.dependency.cleanConsumed
import shmp.simulation.space.resource.dependency.cleanNeeded
import shmp.visualizer.addResourceOnTile
import shmp.visualizer.command.Command
import shmp.visualizer.command.CommandExecutor
import shmp.visualizer.command.EnvironmentCommand.*
import java.util.*


class TextEcosystemExecutor : CommandExecutor<TextEcosystemVisualizer<*>> {
    override fun tryRun(line: String, command: Command, visualizer: TextEcosystemVisualizer<*>): Boolean {
        val splitCommand = line.split(" ")

        visualizer.apply {
            val world = controller.world
            val map = world.map
            when (command) {
                Plates -> printMap { platesMapper(map.tectonicPlates, it) }
                Temperature -> printMap { temperatureMapper(it) }
                Wind -> printMap { windMapper(it) }
                TerrainLevel -> printMap { levelMapper(it) }
                Vapour -> printMap { vapourMapper(it) }
                TileTag -> printMap { tileTagMapper(splitCommand[1], it) }
                Resource ->
                    try {
                        val resource = world.resourcePool.getBaseNameOrNull(splitCommand[1])
                                ?: world.resourcePool.all.first {
                                    it.baseName.toLowerCase() == splitCommand[1].toLowerCase()
                                }
                        printResource(resource)
                    } catch (e: NoSuchElementException) {
                        resourceSymbols.entries
                                .filter { it.value == splitCommand[1] }
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
                ResourceType ->
                    if (ResourceType.values().toList().any { it.toString() == splitCommand[1] }) {
                        val type = ResourceType.valueOf(splitCommand[1])
                        printMap { resourceTypeMapper(type, it) }
                    } else
                        println("Unknown type - " + splitCommand[1])
                ResourceOwner -> printMap { resourceOwnerMapper(splitCommand[1], it) }
                AliveResourcesAmount -> println(aliveResourcesCounter(world))
                AllPresentResources -> println(allResourcesCounter(
                        world,
                        splitCommand.size > 1 && splitCommand[1] == "f"
                ))
                AllPossibleResources -> println(visualizer.printedResources())
                Tile -> map[splitCommand[0].toInt(), splitCommand[1].toInt() + mapPrintInfo.cut]
                        ?.let {
                            printTile(it)
                        } ?: print("No such Tile")
                Tiles -> {
                    val top = splitCommand[0].toInt()
                    val left = splitCommand[1].toInt()
                    val bottom = splitCommand[2].toInt()
                    val right = splitCommand[3].toInt()

                    val tiles = mutableListOf<shmp.simulation.space.tile.Tile>()

                    for (i in top..bottom)
                        for (j in left..right)
                            map[i, j + mapPrintInfo.cut]?.let {
                                tiles += it
                            }

                    printTiles(tiles)
                }
                ResourceDensity -> printMap { resourceDensityMapper(data.tileResourceCapacity, it) }
                PinResources -> {
                    val resourceQuery = splitCommand[1]
                    val mapperFun = { t: shmp.simulation.space.tile.Tile ->
                        resourceSubstringMapper(resourceQuery, t, MARK_COLOR + splitCommand[2])
                    }
                    val minOrder = visualizer.tileMappers.map { it.order }
                            .minOrNull()
                            ?: 0
                    val mapper = TileMapper(mapperFun, minOrder - 1, "Resource $resourceQuery")

                    visualizer.addTileMapper(mapper)
                    printMap(mapperFun)
                    println(briefPrintResourcesWithSubstring(map, splitCommand[1]))
                    println("Added mapper for this resource")
                }
                UnpinResources -> visualizer.removeTileMapperByName("Resource ${splitCommand[1]}")
                CleanConsumers -> {
                    cleanConsumed()
                    cleanNeeded()
                    println("Consumers cleared")
                }
                Events -> {
                    var amount = 100
                    var drop = splitCommand[0].length + 1
                    if (splitCommand[0][0] != 'e') {
                        amount = splitCommand[0].toInt()
                        drop += splitCommand[1].length + 1
                    }
                    if (drop >= line.length) {
                        return true
                    }
                    val regexp = line.substring(drop)
                    println(printRegexEvents(
                            controller.interactionModel.eventLog.lastEvents,
                            amount,
                            regexp
                    ))
                }
                ShowMap -> printMap { "" }
                LegendOn -> visualizer.showLegend = true
                LegendOff -> visualizer.showLegend = false
                Exit -> return true
                AddResource -> addResourceOnTile(
                        map[splitCommand[0].toInt(), splitCommand[1].toInt()],
                        splitCommand[2],
                        world.resourcePool
                )
                GeologicalTurn -> {
                    controller.geologicTurn()
                    print()
                }
                Turner -> line.toIntOrNull()
                        ?.let { launchTurner(it) }
                        ?: println("Wrong number format for amount of turns")
                Turn -> {
                    controller.turn()
                    print()
                }
                PrintStep -> {
                    splitCommand[1].toIntOrNull()
                            ?.let { visualizer.printTurnStep = it }
                            ?: println("Wrong number format for amount of turns")
                    println("Print step number changed")
                }
                AddPrintCommand -> {
                    val suffixCommand = splitCommand.drop(1).joinToString(" ")
                    visualizer.printCommands += suffixCommand
                    println("Command '$suffixCommand' added on print")
                }
                ClearPrintCommands -> {
                    visualizer.printCommands.clear()
                    println("All print commands cleared")
                }
                else -> {
                    println("Unknown command")
                    return false
                }
            }
        }
        return true
    }
}
