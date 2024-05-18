package io.tashtabash.visualizer.command

import io.tashtabash.visualizer.text.TextEcosystemVisualizer


enum class EnvironmentCommand(command: String, override val description: String) : TextCommand {
    Plates("plates", "Display tectonic plates"),
    TileTag("tt \\w+", "Display tile tags"),
    Tile("\\d+ \\d+", "Display tile info"),
    Tiles("\\d+ \\d+ \\d+ \\d+", "Display info for a rectangle of tiles"),
    Temperature("temperature", "Display temperature across the map"),
    Wind("wind", "Display wind directions across the map"),
    Flow("flow", "Display flow directions across the map"),
    TerrainLevel("level", "Display terrain level across the map"),
    Vapour("vapour", "Display vapour amount across the map"),
    Resource("r \\w+", "Display resource description"),
    ResourceSubstring("r' \\w+", "Display all resources containing the given substring"),
    ResourceSubstringOnTile("\\d+ \\d+ r' \\w+", "Display all resources containing the given substring on this tile"),
    ResourceType("rt \\w+", "Display resources of the given type across the map"),
    ResourceOwner("ro \\w+", "Display resources with the given owner across the map"),
    AliveResourcesAmount("rr", "Display all basic resources amounts on the map"),
    AllPresentResources("rrr( f)?", "Display all resources amounts on the map (f - cluster resources from different owners)"),
    AllPossibleResources("rrrr", "Display all resources which can exist"),
    ResourceDensity("rd", "Display resource density across the map"),
    PinResources("pin!? \\w+ .", "Always display the resource on the map"),
    UnpinResources("unpin \\w+", "Stop displaying the resource on the map"),
    CleanConsumers("cln cons", "Clean resource statistics for consumers and consumed"),
    Events("(\\d+ )?e ?.*", "Display events: [numberOfEvents ]e <query>; default numberOfEvents = 100"),
    ShowMap("[mM]", "Display map"),
    LegendOn("legend on", "Enable displaying legend"),
    LegendOff("legend off", "Disable displaying legend"),
    Exit("EXIT", "Exit simulation"),
    AddResource("\\d+ \\d+ \\w+", "Add a resource on the tile"),
    GeologicalTurn("Geo", "Display the geological turn"),
    Turn("", "Make one simulation turn"),
    Turner("\\d+", "Make <N> simulation turns"),
    DisplayFrequency("step \\d+", "Set update display frequency"),
    AddDisplayCommand("pc .+", "Add a command to be executed at every update display"),
    ClearDisplayCommands("cln pc", "Remove all added commands that were added to be executed at every update display");

    override val pattern = Regex(command)
}

fun registerEnvironmentalCommands(commandManager: CommandManager<out TextEcosystemVisualizer<*>>, handler: CommandExecutor<TextEcosystemVisualizer<*>>) {
    commandManager.registerCommands(EnvironmentCommand.values().toList())
    commandManager.registerHandler(handler)
    commandManager.defaultCommand = EnvironmentCommand.Turn
}
