package shmp.visualizer.command

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
    AllPresentResources("rrr( f)?"),
    AllPossibleResources("rrrr"),
    ResourceDensity("rd"),
    PinResources("pin \\w+ ."),
    UnpinResources("unpin \\w+"),
    Events("(\\d+ )?e ?.*"),
    ShowMap("[mM]"),
    LegendOn("legend on"),
    LegendOff("legend off"),
    Exit("EXIT"),
    AddResource("\\d+ \\d+ \\w+"),
    GeologicalTurn("Geo"),
    Turn(""),
    Turner("\\d+"),
    PrintStep("print step \\d+");

    override val pattern = Regex(command)
}

fun registerEnvironmentalCommands(commandManager: CommandManager<out TextEcosystemVisualizer>, handler: CommandHandler<TextEcosystemVisualizer>) {
    commandManager.registerCommands(EnvironmentCommand.values().toList())
    commandManager.registerHandler(handler)
    commandManager.defaultCommand = EnvironmentCommand.Turn
}
