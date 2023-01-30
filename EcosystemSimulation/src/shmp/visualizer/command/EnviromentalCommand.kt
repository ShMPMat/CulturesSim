package shmp.visualizer.command

import shmp.visualizer.text.TextEcosystemVisualizer


enum class EnvironmentCommand(command: String, override val description: String) : Command {
    Plates("plates", "Display tectonic plates"),
    TileTag("tt \\w+", "Display tile tags"),
    Tile("\\d+ \\d+", "Display tile info"),
    Tiles("\\d+ \\d+ \\d+ \\d+", "Display info for a rectangle of tiles"),
    Temperature("temperature", "Display temperature across the map"),
    Wind("wind", "Display wind directions across the map"),
    TerrainLevel("level", "Display terrain level across the map"),
    Vapour("vapour", "Display vapour amount across the map"),
    Resource("r \\w+", "Display resource description"),
    ResourceSubstring("r' \\w+", "Display all resources containing the given substring"),
    ResourceSubstringOnTile("\\d+ \\d+ r' \\w+", "Display all resources containing the given substring on this tile"),
    ResourceType("rt \\w+", "Display resources of the given type across the map"),
    ResourceOwner("ro \\w+", "Display resources with the given owner across the map"),
    BasicResourcesAmount("rr", "Display all basic resources amounts on the map"),
    AllPresentResources("rrr( f)?", "Display all resources amounts on the map"),
    AllPossibleResources("rrrr", "Display all resources amounts on the map"),
    ResourceDensity("rd", "Display resource density across the map"),
    PinResources("pin \\w+ .", "Always display the resource on the map"),
    UnpinResources("unpin \\w+", "Stop displaying the resource on the map"),
    CleanConsumers("cln cons", "Clean resource statistics for consumers and consumed"),
    Events("(\\d+ )?e ?.*", "Display events"),
    ShowMap("[mM]", "Display map"),
    LegendOn("legend on", "Enable displaying legend"),
    LegendOff("legend off", "Disable displaying legend"),
    Exit("EXIT", "Exit simulation"),
    AddResource("\\d+ \\d+ \\w+", "Add a resource on the tile"),
    GeologicalTurn("Geo", "Display the geological turn"),
    Turn("", "Make one simulation turn"),
    Turner("\\d+", "Make <N> simulation turns"),
    PrintStep("step \\d+", "Set update display frequency"),
    AddPrintCommand("pc .+", "Add a command to be executed at every update display"),
    ClearPrintCommands("cln pc", "Remove all added commands that were added to be executed at every update display");

    override val pattern = Regex(command)
}

fun registerEnvironmentalCommands(commandManager: CommandManager<out TextEcosystemVisualizer<*>>, handler: CommandExecutor<TextEcosystemVisualizer<*>>) {
    commandManager.registerCommands(EnvironmentCommand.values().toList())
    commandManager.registerHandler(handler)
    commandManager.defaultCommand = EnvironmentCommand.Turn
}
