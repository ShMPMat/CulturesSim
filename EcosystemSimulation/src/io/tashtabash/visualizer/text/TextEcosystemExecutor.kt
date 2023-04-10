package io.tashtabash.visualizer.text

import io.tashtabash.simulation.space.SpaceData.data
import io.tashtabash.simulation.space.resource.ResourceType
import io.tashtabash.simulation.space.resource.dependency.cleanConsumed
import io.tashtabash.simulation.space.resource.dependency.cleanNeeded
import io.tashtabash.visualizer.addResourceOnTile
import io.tashtabash.visualizer.command.Command
import io.tashtabash.visualizer.command.CommandExecutor
import io.tashtabash.visualizer.command.EnvironmentCommand.*
import io.tashtabash.visualizer.command.ExecutionResult
import java.util.*
import java.util.regex.PatternSyntaxException


class TextEcosystemExecutor : CommandExecutor<TextEcosystemVisualizer<*>> {
    override fun tryRun(line: String, command: Command, visualizer: TextEcosystemVisualizer<*>): ExecutionResult {
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
                                    it.baseName.lowercase() == splitCommand[1].lowercase()
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
                    } else {
                        println("Unknown type - " + splitCommand[1])

                        return ExecutionResult.Terminate
                    }
                ResourceOwner -> printMap { resourceOwnerMapper(splitCommand[1], it) }
                AliveResourcesAmount -> println(aliveResourcesCounter(world))
                AllPresentResources -> println(
                        allResourcesCounter(world, splitCommand.getOrNull(1) == "f")
                )
                AllPossibleResources -> println(visualizer.printedResources())
                Tile -> map[splitCommand[0].toInt(), splitCommand[1].toInt() + mapPrintInfo.cut]
                        ?.let {
                            printTile(it)
                        } ?: run {
                            print("No such Tile")

                            return ExecutionResult.Terminate
                        }
                Tiles -> {
                    val top = splitCommand[0].toInt()
                    val left = splitCommand[1].toInt()
                    val bottom = splitCommand[2].toInt()
                    val right = splitCommand[3].toInt()

                    val tiles = mutableListOf<io.tashtabash.simulation.space.tile.Tile>()

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
                    val mapperFun = { t: io.tashtabash.simulation.space.tile.Tile ->
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

                    try {
                        val regexString =  if (drop < line.length)
                            line.substring(drop)
                        else ".*"

                        val regexp = regexString.toRegex()
                        println(printRegexEvents(
                                controller.interactionModel.eventLog.lastEvents,
                                amount,
                                regexp
                        ))
                    } catch (e: PatternSyntaxException) {
                        println("Invalid regex pattern")
                        return ExecutionResult.Invalid
                    }
                }
                ShowMap -> printMap { "" }
                LegendOn -> visualizer.showLegend = true
                LegendOff -> visualizer.showLegend = false
                Exit -> {
                    println("Terminating the simulation...")

                    return ExecutionResult.Terminate
                }
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
                        ?: run {
                            println("Wrong number format for amount of turns")

                            return ExecutionResult.Invalid
                        }
                Turn -> {
                    controller.turn()
                    print()
                }
                DisplayFrequency -> {
                    splitCommand[1].toIntOrNull()
                            ?.let { visualizer.printTurnStep = it }
                            ?: run {
                                println("Wrong number format for amount of turns")

                                return ExecutionResult.Invalid
                            }
                    println("Print step number changed")
                }
                AddDisplayCommand -> {
                    val suffixCommand = splitCommand.drop(1).joinToString(" ")
                    visualizer.printCommands += suffixCommand
                    println("Command '$suffixCommand' added on print")
                }
                ClearDisplayCommands -> {
                    visualizer.printCommands.clear()
                    println("All print commands cleared")
                }
                else -> {
                    println("Unknown command")
                    return ExecutionResult.NotFound
                }
            }
        }
        return ExecutionResult.NotFound
    }
}
