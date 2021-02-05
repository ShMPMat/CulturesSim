package shmp.visualizer.text

import shmp.simulation.Controller
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.tile.Tile
import shmp.visualizer.command.registerCultureCommands
import shmp.visualizer.lastClaimedTiles
import shmp.visualizer.printedConglomerates
import shmp.visualizer.printinfo.ConglomeratePrintInfo
import java.io.FileReader
import java.util.*


open class TextCultureVisualizer(controller: Controller) : TextEcosystemVisualizer(controller) {
    private var groupInfo = ConglomeratePrintInfo(mutableListOf())
    private var lastClaimedTiles: Map<Group, Set<Tile>> = mutableMapOf()
    private var lastClaimedTilesPrintTurn = 0

    internal override fun initialize() {
        registerCultureCommands(commandManager, TextCultureHandler)
        addTileMapper(TileMapper({ cultureTileMapper(lastClaimedTiles, groupInfo, it) }, 5))

        super.initialize()
    }

    override fun print() {
        lastClaimedTiles = interactionModel.eventLog.lastClaimedTiles(lastClaimedTilesPrintTurn)
        lastClaimedTilesPrintTurn = world.getTurn()
        println(printedConglomerates(world.groups, groupInfo))
        super.print()
    }

    internal override fun run() {
        readSymbols()
        super.run()
    }

    private fun readSymbols() {
        val s = Scanner(FileReader("SupplementFiles/Symbols/SymbolsLibrary"))
        val l = mutableListOf<String>()
        while (s.hasNextLine())
            l.add(s.nextLine())
        groupInfo = ConglomeratePrintInfo(l)
    }
}
