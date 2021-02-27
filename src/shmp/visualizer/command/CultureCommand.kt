package shmp.visualizer.command

import shmp.visualizer.Visualizer


enum class CultureCommand(command: String) : Command {
    Conglomerate("^G\\d+_?\\d*"),
    GroupTileReach("^G\\d+ r"),
    GroupProduced("^G\\d+ p"),
    GroupRelations("^G\\d+ G\\d+"), Tile("\\d+ \\d+"),
    GroupPotentials("^G\\d+ p \\d+"),
    MeaningfulResources("meaning"),
    ArtificialResources("artificial"),
    GroupStatistics("gstat"),
    Aspects("a \\w+"),
    Strata("s \\w+"),
    AddAspect("^G\\d+ \\w+"),
    AddWant("^want G\\d+ \\w+");

    override val pattern = Regex(command)
}

fun <E: Visualizer<E>> registerCultureCommands(commandManager: CommandManager<E>, handler: CommandHandler<E>) {
    commandManager.registerCommands(CultureCommand.values().toList())
    commandManager.registerHandler(handler)
}
