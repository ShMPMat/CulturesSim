package visualizer

/**
 * Represents commands which can be given to visualizer.
 */
enum class Command(command: String) {
    /**
     * Command for making turns until something important happens.
     */
    IdleGo("go"),  //TODO does it work still even?
    /**
     * Command for printing group information.
     */
    Group("^G\\d+"),
    /**
     * Command for printing tile information.
     */
    Tile("\\d+ \\d+"),
    Plates("plates"),
    Temperature("temperature"),
    Wind("wind"),
    TerrainLevel("level"),
    GroupPotentials("^G\\d+ p \\d+"),
    Vapour("vapour"), /**
     * Command for printing resource information.
     */
    Resource("r \\w+"),
    MeaningfulResources("meaning"),
    ArtificialResources("artificial"),
    Aspects("a \\w+"), /**
     * Command for printing map.
     */
    Map("[mM]"),
    /**
     * Command for exiting simulation.
     */
    Exit("EXIT"),
    /**
     * Command for adding Aspect for a group.
     */
    AddAspect("^G\\d+ \\w+"),
    AddWant("^want G\\d+ \\w+"),
    AddResource("\\d+ \\d+ \\w+"),
    GeologicalTurn("Geo"),
    Turn(""),
    Turner("\\d+");

    var pattern = Regex(command)
}

/**
 * Function returning a command represented in the line.
 */
fun getCommand(line: String): Command {
    for (command in Command.values())
        if (command.pattern.matches(line)) return command
    return Command.Turn
}