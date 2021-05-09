package shmp.visualizer.command

import shmp.visualizer.Visualizer
import shmp.visualizer.text.TextCultureHandler
import shmp.visualizer.text.TextCultureVisualizer
import shmp.visualizer.text.TextEcosystemVisualizer


enum class CultureCommand(command: String) : Command {
    Conglomerate("^G\\d+_?\\d*"),
    GroupTileReach("^G\\d+ r"),
    GroupProduced("^G\\d+ p"),
    GroupRelations("^G\\d+ G\\d+"),
    GroupPotentials("^G\\d+ p \\d+"),
    MeaningfulResources("meaning"),
    ArtificialResources("artificial"),
    GroupStatistics("gstat"),
    Aspects("a \\w+"),
    CultureAspects("ca \\w+"),
    Strata("s \\w+"),
    AddAspect("^G\\d+ \\w+"),
    AddWant("^want G\\d+ \\w+");

    override val pattern = Regex(command)
}

fun registerCultureCommands(commandManager: CommandManager<out TextCultureVisualizer>, handler: CommandHandler<TextCultureVisualizer>) {
    commandManager.registerCommands(CultureCommand.values().toList())
    commandManager.registerHandler(handler)
}
