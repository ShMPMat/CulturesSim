package shmp.visualizer.command

import shmp.visualizer.Visualizer


enum class EnvironmentCommand(command: String) : Command {
    Plates("plates"),
    TileTag("tt \\w+"),
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

fun <E: Visualizer<E>> registerEnvironmentalCommands(commandManager: CommandManager<E>, handler: CommandHandler<E>) {
    commandManager.registerCommands(EnvironmentCommand.values().toList())
    commandManager.registerHandler(handler)
    commandManager.defaultCommand = EnvironmentCommand.Turn
}
