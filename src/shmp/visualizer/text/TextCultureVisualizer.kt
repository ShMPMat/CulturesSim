package shmp.visualizer.text

import shmp.simulation.CulturesController
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.tile.Tile
import shmp.visualizer.command.CommandManager
import shmp.visualizer.command.registerCultureCommands
import shmp.visualizer.lastClaimedTiles
import shmp.visualizer.printinfo.ConglomeratePrintInfo
import java.io.FileReader
import java.util.*


open class TextCultureVisualizer(
        override val controller: CulturesController,
        private val defaultManager: CommandManager<TextCultureVisualizer> = CommandManager(TextPassHandler()),
        private val commandManager: CommandManager<out TextCultureVisualizer> = defaultManager
) : TextEcosystemVisualizer(controller, CommandManager(TextPassHandler()), commandManager) {
    private var groupInfo = ConglomeratePrintInfo(mutableListOf())
    private var lastClaimedTiles: Map<Group, Set<Tile>> = mutableMapOf()
    private var lastClaimedTilesPrintTurn = 0

    private val world
        get() = controller.world

    override fun initialize() {
        registerCultureCommands(commandManager, TextCultureHandler)
        addTileMapper(TileMapper({ cultureTileMapper(lastClaimedTiles, groupInfo, it) }, 5))

        super.initialize()

        controller.initializeThird()
    }

    override fun print() {
        lastClaimedTiles = interactionModel.eventLog.lastClaimedTiles(lastClaimedTilesPrintTurn)
        lastClaimedTilesPrintTurn = world.getTurn()
        println(printedConglomerates(world.groups, groupInfo))
        println("Conglomerates overall: ${world.groups.size}")
        super.print()
    }

    override fun run() {
        readSymbols()
        super.run()
    }

    override fun handleCommand(line: String) {
        defaultManager.handleCommand(line, this)
    }

    private fun readSymbols() {
        val s = Scanner(FileReader(this::class.java.classLoader.getResource("Symbols/SymbolsLibrary")!!.path))
        val l = mutableListOf<String>()
        while (s.hasNextLine())
            l.add(s.nextLine())
        groupInfo = ConglomeratePrintInfo(l)
    }
}
