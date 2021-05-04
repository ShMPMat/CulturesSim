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
import java.util.function.Function


open class TextEcosystemVisualizer(open val controller: Controller<out World>) : Visualizer {
    /**
     * Symbols for representation of resource on the Map.
     */
    var resourceSymbols: MutableMap<Resource, String> = HashMap()
    var mapPrintInfo: MapPrintInfo
    private val world
        get() = controller.world

    protected val interactionModel: InteractionModel<out World>
        get() = controller.interactionModel

    private val map: WorldMap
        get() = world.map

    private var currentTurner: Turner? = null
    private var turnerThread: Thread? = null
    val commandManager = CommandManager(TextPassHandler<TextEcosystemVisualizer>())
    private val tileMappers: MutableList<TileMapper> = ArrayList()


    init {
        Controller.visualizer = this
        mapPrintInfo = MapPrintInfo()
    }

    open fun initialize() {
        registerEnvironmentalCommands(commandManager, TextEcosystemHandler())
        addTileMapper(TileMapper({ t: Tile? -> ecosystemTypeMapper(resourceSymbols, t!!) }, 10))
        print()
        controller.initializeFirst()
        print()
        controller.initializeSecond()
        print()
        mapPrintInfo.computeCut(map)
    }

    /**
     * Fills Symbol fields for map drawing.
     *
     * @throws IOException when files with symbols isn't found.
     */
    private fun readSymbols() {
        val s = Scanner(FileReader("SupplementFiles/Symbols/SymbolsResourceLibrary"))
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
        val main = StringBuilder()
        main.append(world.getStringTurn()).append("\n")
        print(main.append(addToRight(
                printedMap { "" },
                addToRight(
                        chompToLines(printedResources().toString(), map.linedTiles.size + 2),
                        printedEvents(interactionModel.eventLog.newEvents, false),
                        false
                ),
                true
        ))
        )
    }

    /**
     * Prints world map.
     *
     * @param condition function which adds an extra layer of information
     * above default map. If function returns non-empty string
     * for a tile, output of the function will be drawn above the tile.
     */
    fun printMap(condition: Function<Tile, String>) {
        print(addToRight(
                printedMap(condition),
                chompToLines(printedResources(), map.linedTiles.size + 2),
                true
        ))
    }

    /**
     * @param mapper function which adds an extra layer of information
     * above default map. If function returns non-empty string
     * for a tile, output of the function will be drawn above the tile.
     */
    private fun printedMap(mapper: Function<Tile, String>): StringBuilder {
        val main = StringBuilder()
        val worldMap = controller.world!!.map
        main.append("  ")
        for (i in worldMap.linedTiles[0].indices) {
            main.append(if (i < 100) " " else i / 100 % 100)
        }
        main.append("\n").append("  ")
        for (i in worldMap.linedTiles[0].indices) {
            main.append(if (i < 10) " " else i / 10 % 10)
        }
        main.append("\n").append("  ")
        for (i in worldMap.linedTiles[0].indices) {
            main.append(i % 10)
        }
        main.append("\n")
        val map = StringBuilder()
        for (i in 0 until data.mapSizeX) {
            var token: String
            map.append(if (i < 10) " $i" else i)
            for (j in 0 until data.mapSizeY) {
                val tile = worldMap.getValue(i, j + mapPrintInfo.cut)
                token = mapper.apply(tile)
                if (token == "") {
                    when (tile.type) {
                        Tile.Type.Ice -> token = "\u001b[47m"
                        Tile.Type.Water -> token = "\u001b[44m"
                        Tile.Type.Woods -> token = "\u001b[42m"
                        Tile.Type.Growth -> token = "\u001b[103m"
                        else -> {}
                    }
                    token += applyMappers(tile)
                }
                map.append(token).append("\u001b[0m")
            }
            map.append("\u001b[0m").append("\n")
        }
        return main.append(map)
    }

    private fun printedResources(): StringBuilder {
        val resources = StringBuilder()
        for (resource in world.resourcePool.all) {
            resources.append("\u001b[31m").append(resourceSymbols[resource]).append(" - ")
                    .append(resource.baseName).append("\n")
        }
        return resources
    }

    private fun printedEvents(events: Collection<Event>, printAll: Boolean): StringBuilder {
        val main = StringBuilder()
        for (event in events) {
            if (printAll || event.type === Type.Death || event.type === Type.ResourceDeath || event.type === Type.DisbandResources) {
                main.append(event).append("\n")
            }
        }
        return main
    }

    fun launchTurner(turnAmount: Int) {
        currentTurner = Turner(turnAmount, controller)
        turnerThread = Thread(currentTurner)
        turnerThread!!.start()
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
                        commandManager.handleCommand(line, this)
                    } else {
                        break
                    }
                }
            }
        } catch (t: Throwable) {
            System.err.println(t.toString())
            for (stackTraceElement in t.stackTrace) {
                System.err.println(stackTraceElement)
            }
        }
    }

    protected fun addTileMapper(mapper: TileMapper) {
        tileMappers.add(mapper)
        tileMappers.sortWith(Comparator.comparingInt(TileMapper::order))
    }

    private fun applyMappers(tile: Tile?): String {
        for (mapper in tileMappers) {
            val result = mapper.mapper.invoke(tile!!)
            if (result != "") {
                return result
            }
        }
        return " "
    }
}
