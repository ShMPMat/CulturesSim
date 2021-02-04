package shmp.visualizer.command

import shmp.visualizer.TextEnvironmentalHandler
import shmp.visualizer.Visualizer


enum class EnvironmentCommand(command: String) : Command {
    Conglomerate("^G\\d+_?\\d*"),
    GroupTileReach("^G\\d+ r"),
    GroupProduced("^G\\d+ p"),
    GroupRelations("^G\\d+ G\\d+"), Tile("\\d+ \\d+"),
    Plates("plates"),
    TileTag("tt \\w+"),
    Temperature("temperature"),
    Wind("wind"),
    TerrainLevel("level"),
    GroupPotentials("^G\\d+ p \\d+"),
    Vapour("vapour"),
    Resource("r \\w+"),
    ResourceSubstring("r' \\w+"),
    ResourceSubstringOnTile("\\d+ \\d+ r' \\w+"),
    ResourceType("rt \\w+"),
    ResourceOwner("ro \\w+"),
    AllResources("rr"),
    ResourceDensity("rd"),
    MeaningfulResources("meaning"),
    ArtificialResources("artificial"),
    Aspects("a \\w+"),
    Strata("s \\w+"),
    Events("(\\d+ )?e ?.*"),
    ShowMap("[mM]"),
    Exit("EXIT"),
    AddAspect("^G\\d+ \\w+"),
    AddWant("^want G\\d+ \\w+"),
    AddResource("\\d+ \\d+ \\w+"),
    GeologicalTurn("Geo"),
    Turn(""),
    Turner("\\d+");

    override val pattern = Regex(command)
}

fun <E: Visualizer<E>> registerEnvironmentalCommands(commandManager: CommandManager<E>, handler: CommandHandler<E>) {
    commandManager.registerCommands(EnvironmentCommand.values().toList())
    commandManager.registerHandler(handler)
    commandManager.defaultCommand = EnvironmentCommand.Turn
}