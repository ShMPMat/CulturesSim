package shmp.visualizer.text

import shmp.simulation.Controller
import shmp.simulation.World
import shmp.simulation.event.Event
import shmp.simulation.event.Type
import shmp.simulation.interactionmodel.InteractionModel
import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.WorldMap
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.tile.Tile
import shmp.utils.addToRight
import shmp.utils.chompToLines
import shmp.visualizer.Turner
import shmp.visualizer.Visualizer
import shmp.visualizer.command.CommandManager
import shmp.visualizer.command.registerEnvironmentalCommands
import shmp.visualizer.printinfo.MapPrintInfo
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


open class TextEcosystemVisualizer(
        open val controller: Controller<out World>,
        private val defaultManager: CommandManager<TextEcosystemVisualizer> = CommandManager(TextPassHandler()),
        private val commandManager: CommandManager<out TextEcosystemVisualizer> = defaultManager
) : Visualizer {
    /**
     * Symbols for representation of resources on the Map.
     */
    var resourceSymbols: MutableMap<Resource, String> = mutableMapOf()
    var mapPrintInfo: MapPrintInfo

    internal var showLegend = false

    private val world
        get() = controller.world

    protected val interactionModel: InteractionModel<out World>
        get() = controller.interactionModel

    private val map: WorldMap
        get() = world.map

    private var currentTurner: Turner? = null
    private var turnerThread: Thread? = null
    internal var printTurnStep = 50

    private val _tileMappers = mutableListOf<TileMapper>()
    val tileMappers: List<TileMapper> = _tileMappers

    init {
        Controller.visualizer = this
        mapPrintInfo = MapPrintInfo()
    }

    open fun initialize() {
        registerEnvironmentalCommands(commandManager, TextEcosystemHandler())
        addTileMapper(TileMapper({ ecosystemTypeMapper(resourceSymbols, it) }, 10))
        println()
        controller.initializeFirst()
        println()
        controller.initializeSecond()
        println()
        mapPrintInfo.computeCut(map)
    }

    /**
     * Fills Symbol fields for map drawing.
     *
     * @throws IOException when files with symbols isn't found.
     */
    private fun readSymbols() {
        val s = Scanner(FileReader(this::class.java.classLoader.getResource("Symbols/SymbolsResourceLibrary")!!.path))
        val symbols = mutableListOf<String>()

        while (s.hasNextLine())
            symbols.add(s.nextLine())
        for ((i, resource) in world.resourcePool.all.withIndex())
            resourceSymbols[resource] = symbols[i % symbols.size]
    }

    /**
     * Prints default map and information output.
     */
    override fun print() {
        println(world.getStringTurn())
        printMap { "" }
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
                token = mapper(tile)
                if (token == "") {
                    when (tile.type) {
                        Tile.Type.Ice -> token = "\u001b[47m"
                        Tile.Type.Water -> token = when (tile.level) {
                            in 0..70 -> "\u001b[48:5:27m"
                            in 71..80 -> "\u001b[48:5:33m"
                            else -> "\u001b[48:5:39m"
                        }
                        Tile.Type.Woods -> token = "\u001b[42m"
                        Tile.Type.Growth -> token = "\u001b[103m"
                        else -> {
                        }
                    }
                    token += applyMappers(tile)
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
        for (resource in world.resourcePool.resources)
            resources.append("\u001b[31m").append(resourceSymbols[resource]).append(" - ")
                    .append(resource.baseName).append("\n")

        resourcesPrinted = resources

        return resources
    }

    private var resourcesPrinted: StringBuilder? = null

    private fun printedEvents(events: Collection<Event>, printAll: Boolean): StringBuilder {
        val main = StringBuilder()
        for (event in events) {
            if (printAll || event.type === Type.Death || event.type === Type.ResourceDeath || event.type === Type.DisbandResources)
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
        println("Turner is asked to stop")
        turnerThread!!.join()
        currentTurner = null
        println("Turner has stopped")
    }

    /**
     * Runs interface for the shmp.simulation control.
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
                    if (line != null) {
                        if (currentTurner != null) {
                            stopTurner()
                            print()
                            continue
                        }
                        handleCommand(line)
                    } else break
                }
            }
        } catch (t: Throwable) {
            System.err.println(t.toString())
            for (stackTraceElement in t.stackTrace) {
                System.err.println(stackTraceElement)
            }
        }
    }

    open fun handleCommand(line: String) {
        defaultManager.handleCommand(line, this)
    }

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
}
