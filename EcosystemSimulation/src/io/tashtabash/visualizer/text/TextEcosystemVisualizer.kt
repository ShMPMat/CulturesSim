package io.tashtabash.visualizer.text

import io.tashtabash.sim.Controller
import io.tashtabash.sim.World
import io.tashtabash.sim.event.*
import io.tashtabash.sim.interactionmodel.InteractionModel
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.utils.addToRight
import io.tashtabash.utils.chompToLines
import io.tashtabash.visualizer.Turner
import io.tashtabash.visualizer.Visualizer
import io.tashtabash.visualizer.command.CommandManager
import io.tashtabash.visualizer.command.ExecutionResult
import io.tashtabash.visualizer.command.registerEnvironmentalCommands
import io.tashtabash.visualizer.printinfo.MapPrintInfo
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


open class TextEcosystemVisualizer<E : World>(
        open val controller: Controller<E>,
        private val defaultManager: CommandManager<TextEcosystemVisualizer<E>> = CommandManager(TextPassExecutor()),
        private val commandManager: CommandManager<out TextEcosystemVisualizer<E>> = defaultManager
) : Visualizer {
    /**
     * Symbols for representation of resources on the Map.
     */
    val resourceSymbols: MutableMap<Resource, String> = mutableMapOf()
    val mapPrintInfo: MapPrintInfo

    internal var showLegend = false

    private val world
        get() = controller.world

    protected val interactionModel: InteractionModel<E>
        get() = controller.interactionModel

    private val map: WorldMap
        get() = world.map

    private var currentTurner: Turner? = null
    private var turnerThread: Thread? = null
    internal var printTurnStep = 50

    private val _tileMappers = mutableListOf<TileMapper>()
    val tileMappers: List<TileMapper> = _tileMappers

    val printCommands = mutableListOf<String>()

    init {
        Controller.visualizer = this
        mapPrintInfo = MapPrintInfo()
    }

    open fun initialize() {
        registerEnvironmentalCommands(commandManager, TextEcosystemExecutor())
        addTileMapper(TileMapper({ ecosystemTypeMapper(resourceSymbols, it) }, 10))
        println()

        controller.runInitSteps()

        mapPrintInfo.computeCut(map)
    }

    /**
     * Fills Symbol fields for map drawing.
     *
     * @throws IOException when files with symbols isn't found.
     */
    private fun readSymbols() {
        val classLoader = Thread.currentThread().contextClassLoader
        val resourceUrl = classLoader.getResource("Symbols/SymbolsResourceLibrary")
                ?: throw IOException("Resource Symbols/SymbolsResourceLibrary not found")
        val s = Scanner(resourceUrl.openStream())
        val symbols = mutableListOf<String>()

        while (s.hasNextLine())
            symbols += s.nextLine()
        for ((i, resource) in world.resourcePool.all.withIndex())
            resourceSymbols[resource] = symbols[i % symbols.size]
    }

    /**
     * Prints default map and information output.
     */
    override fun print() {
        println(world.turn)
        printMap { "" }

        //Results are ignored
        for (command in printCommands)
            handleCommand(command)
    }

    /**
     * Prints world map.
     *
     * @param condition function which adds an extra layer of information
     * above default map. If function returns non-empty string
     * for a tile, output of the function will be drawn above the tile.
     */
    fun printMap(condition: (Tile) -> String) {
        val printedMap = printedMap(condition)

        val resultPrint = if (showLegend) {
            val printedResources = chompToLines(printedResources(), map.linedTiles.size + 2)

            addToRight(printedMap, printedResources, true)
        } else printedMap

        print(resultPrint)
    }

    /**
     * @param mapper function which adds an extra layer of information
     * above default map. If function returns non-empty string
     * for a tile, output of the function will be drawn above the tile.
     */
    private fun printedMap(mapper: (Tile) -> String): StringBuilder {
        val main = StringBuilder("  ")
        val worldMap = map

        for (i in worldMap.linedTiles[0].indices)
            main.append(if (i < 100) " " else i / 100 % 100)
        main.append("\n").append("  ")
        for (i in worldMap.linedTiles[0].indices)
            main.append(if (i < 10) " " else i / 10 % 10)
        main.append("\n").append("  ")
        for (i in worldMap.linedTiles[0].indices)
            main.append(i % 10)
        main.append("\n")

        val map = StringBuilder()
        for (i in 0 until data.mapSizeX) {
            var token: String
            map.append(if (i < 10) " $i" else i)
            for (j in 0 until data.mapSizeY) {
                val tile = worldMap.getValue(i, j + mapPrintInfo.cut)
                token = mapper(tile).takeIf { it != "" } ?: applyMappers(tile)
                if (token == " ") {
                    token = ""
                    when (tile.type) {
                        Tile.Type.Ice -> token = "\u001b[47m"
                        Tile.Type.Water -> token = when (tile.level) {
                            in 0..70 -> "\u001b[48:5:27m"
                            in 71..80 -> "\u001b[48:5:33m"
                            in 81..90 -> "\u001b[48:5:39m"
                            in 91..100 -> "\u001b[48:5:45m"
                            else -> "\u001b[48:5:51m"
                        }
                        Tile.Type.Woods,
                        Tile.Type.Mountain -> {
                            val treeAmount = tile.resourcePack.getAmount { it.tags.any { t -> t.name == "Tree" } }

                            token = when (treeAmount) {
                                0 -> ""
                                in 1..1000 -> "\u001b[48:5:46m"
                                in 1001..10000 -> "\u001b[48:5:40m"
                                in 10001..100000 -> "\u001b[48:5:34m"
                                in 100001..1000000 -> "\u001b[48:5:28m"
                                else -> "\u001b[48:5:22m"
                            }
                        }
                        Tile.Type.Growth -> token = "\u001b[103m"
                        else -> {}
                    }
                    token += if (tile.level >= 110) {
                        val levelPrint = if (tile.level > 130) "\u001b[43m" else ""
                        val snowPrint =
                                if (tile.resourcePack.contains(data.resourcePool.getBaseName("Snow")))
                                    "\u001b[30m"
                                else "\u001b[93m"
                        "$levelPrint$snowPrint^"
                    } else if (tile.level > 105) "~"
                    else " "
                }
                map.append(token).append("\u001b[0m")
            }
            map.append("\u001b[0m").append("\n")
        }
        return main.append(map)
    }

    internal fun printedResources(): StringBuilder {
        resourcesPrinted?.let {
            return it
        }

        val resources = StringBuilder()
        for (resource in world.resourcePool.all)
            resources.append("\u001b[31m").append(resourceSymbols[resource]).append(" - ")
                    .append(resource.baseName).append("\n")

        resourcesPrinted = resources

        return resources
    }

    private var resourcesPrinted: StringBuilder? = null

    private fun printedEvents(events: Collection<Event>, printAll: Boolean): StringBuilder {
        val main = StringBuilder()
        for (event in events) {
            if (printAll || event.type in listOf(Death, ResourceDeath, DisbandResources))
                main.append(event).append("\n")
        }
        return main
    }

    fun launchTurner(turnAmount: Int) {
        currentTurner = Turner(turnAmount, printTurnStep, controller)
        turnerThread = Thread(currentTurner)
        turnerThread?.start()
    }

    @Throws(InterruptedException::class)
    private fun stopTurner() {
        currentTurner!!.isAskedToStop.set(true)
        println("Pausing the simulation")
        turnerThread!!.join()
        currentTurner = null
        println("The simulation is paused")
    }

    /**
     * Runs interface for the io.tashtabash.simulation control.
     */
    open fun run() {
        try {
            BufferedReader(InputStreamReader(System.`in`)).use { br ->
                readSymbols()
                print()
                var isFirstTurn = true
                while (true) {
                    val line = if (isFirstTurn) "10000" else br.readLine()
                    isFirstTurn = false
                    line ?: break

                    if (currentTurner != null) {
                        stopTurner()
                        print()
                        continue
                    }

                    val executionResult = handleCommand(line)
                    if (executionResult == ExecutionResult.Terminate)
                        break
                }
            }
        } catch (t: Throwable) {
            System.err.println(t.toString())
            for (stackTraceElement in t.stackTrace) {
                System.err.println(stackTraceElement)
            }
        }
    }

    open fun handleCommand(line: String) =
            defaultManager.handleCommand(line, this)

    fun addTileMapper(mapper: TileMapper) {
        _tileMappers.add(mapper)
        _tileMappers.sortWith(Comparator.comparingInt(TileMapper::order))
    }

    fun removeTileMapperByName(mapperName: String) {
        _tileMappers.removeIf { it.name == mapperName }
    }

    private fun applyMappers(tile: Tile?): String {
        for (mapper in _tileMappers) {
            val result = mapper.mapper.invoke(tile!!)
            if (result != "")
                return result
        }
        return " "
    }

    fun findTile(x: String, y: String) =
        findTile(x.toInt(), y.toInt())

    fun findTile(x: Int, y: Int) =
        controller.world.map[x, y + mapPrintInfo.cut]
}
