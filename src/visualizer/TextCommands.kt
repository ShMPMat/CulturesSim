package visualizer


//Commands which can be given to visualizer.
enum class Command(command: String) {
    Conglomerate("^G\\d+_?\\d*"),
    GroupTileReach("^G\\d+ r"),
    GroupProduced("^G\\d+ p"),
    GroupRelations("^G\\d+ G\\d+"),
    Tile("\\d+ \\d+"),
    Plates("plates"),
    TileTag("tt \\w+"),
    Temperature("temperature"),
    Wind("wind"),
    TerrainLevel("level"),
    GroupPotentials("^G\\d+ p \\d+"),
    Vapour("vapour"),
    Resource("r \\w+"),
    ResourceSubstring("r' \\w+"),
    ResourceType("rt \\w+"),
    ResourceOwner("ro \\w+"),
    AllResources("rr"),
    ResourceDensity("rd"),
    MeaningfulResources("meaning"),
    ArtificialResources("artificial"),
    Aspects("a \\w+"),
    Events("e ?.*"),
    Map("[mM]"),
    Exit("EXIT"),
    AddAspect("^G\\d+ \\w+"),
    AddWant("^want G\\d+ \\w+"),
    AddResource("\\d+ \\d+ \\w+"),
    GeologicalTurn("Geo"),
    Turn(""),
    Turner("\\d+");

    var pattern = Regex(command)
}


fun getCommand(line: String): Command {
    for (command in Command.values())
        if (command.pattern.matches(line)) return command
    return Command.Turn
}
