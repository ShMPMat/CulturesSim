package shmp.visualizer.text

import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.resource.ResourceType
import shmp.visualizer.*
import shmp.visualizer.command.Command
import shmp.visualizer.command.CommandHandler
import shmp.visualizer.command.EnvironmentCommand.*
import shmp.visualizer.command.EnvironmentCommand.Turner
import java.util.*


object TextEnvironmentalHandler: CommandHandler<TextEcosystemVisualizer> {
    override fun tryRun(line: String, command: Command, visualizer: TextEcosystemVisualizer): Boolean {
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
                    println("Unknown type - " + splitCommand[1])
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
                ShowMap -> printMap { "" }
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
                Turner -> line.toIntOrNull()?.let {
                    launchTurner(it)
                } ?: println("Wrong number format for amount of turns")
                Turn -> {
                    controller.turn()
                    print()
                }
                else -> return false
            }
        }
        return true
    }
}