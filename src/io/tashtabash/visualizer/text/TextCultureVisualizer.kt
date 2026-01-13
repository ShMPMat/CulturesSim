package io.tashtabash.visualizer.text

import io.tashtabash.sim.CulturesController
import io.tashtabash.sim.CulturesWorld
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.visualizer.command.CommandManager
import io.tashtabash.visualizer.command.registerCultureCommands
import io.tashtabash.visualizer.lastClaimedTiles
import io.tashtabash.visualizer.printinfo.ConglomeratePrintInfo
import java.io.IOException
import java.util.*


open class TextCultureVisualizer(
        override val controller: CulturesController,
        private val defaultManager: CommandManager<TextCultureVisualizer> = CommandManager(TextPassExecutor()),
        private val commandManager: CommandManager<out TextCultureVisualizer> = defaultManager
) : TextEcosystemVisualizer<CulturesWorld>(controller, CommandManager(TextPassExecutor()), commandManager) {
    private var groupInfo = ConglomeratePrintInfo(mutableListOf())
    private var lastClaimedTiles: Map<Group, Set<Tile>> = mutableMapOf()
    private var lastClaimedTilesPrintTurn = 0

    private val world
        get() = controller.world

    override fun initialize() {
        registerCultureCommands(commandManager, TextCultureExecutor)
        addTileMapper(TileMapper({ cultureTileMapper(lastClaimedTiles, groupInfo, it) }, 5))

        super.initialize()
    }

    override fun print() {
        lastClaimedTiles = interactionModel.eventLog.lastClaimedTiles(lastClaimedTilesPrintTurn)
        lastClaimedTilesPrintTurn = world.getTurn()
        println(printedConglomerates(world.conglomerates, groupInfo))
        println("Conglomerates overall: ${world.conglomerates.size}")
        super.print()
    }

    override fun run() {
        readSymbols()
        super.run()
    }

    override fun handleCommand(line: String) =
            defaultManager.handleCommand(line, this)

    private fun readSymbols() {
        val classLoader = Thread.currentThread().contextClassLoader
        val resourceUrl = classLoader.getResource("Symbols/SymbolsLibrary")
                ?: throw IOException("Symbols/SymbolsLibrary not found")
        val s = Scanner(resourceUrl.openStream())
        val l = mutableListOf<String>()
        while (s.hasNextLine())
            l += s.nextLine()
        groupInfo = ConglomeratePrintInfo(l)
    }
}
