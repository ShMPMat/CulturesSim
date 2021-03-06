package shmp.visualizer.command

import shmp.visualizer.Visualizer
import shmp.visualizer.text.TextEcosystemVisualizer


enum class EnvironmentCommand(command: String) : Command {
    Plates("plates"),
    TileTag("tt \\w+"),
    Tile("\\d+ \\d+"),
    Temperature("temperature"),
    Wind("wind"),
    TerrainLevel("level"),
    Vapour("vapour"),
    Resource("r \\w+"),
    ResourceSubstring("r' \\w+"),
    ResourceSubstringOnTile("\\d+ \\d+ r' \\w+"),
    ResourceType("rt \\w+"),
    ResourceOwner("ro \\w+"),
    AllBasicResources("rr"),
    AllResources("rrr( f)?"),
    ResourceDensity("rd"),
    Events("(\\d+ )?e ?.*"),
    ShowMap("[mM]"),
    Exit("EXIT"),
    AddResource("\\d+ \\d+ \\w+"),
    GeologicalTurn("Geo"),
    Turn(""),
    Turner("\\d+");

    override val pattern = Regex(command)
}

fun registerEnvironmentalCommands(commandManager: CommandManager<out TextEcosystemVisualizer>, handler: CommandHandler<TextEcosystemVisualizer>) {
    commandManager.registerCommands(EnvironmentCommand.values().toList())
    commandManager.registerHandler(handler)
    commandManager.defaultCommand = EnvironmentCommand.Turn
}
