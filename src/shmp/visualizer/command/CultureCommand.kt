package shmp.visualizer.command

import shmp.visualizer.text.TextCultureVisualizer


enum class CultureCommand(command: String, override val description: String) : Command {
    Conglomerate("^G\\d+_?\\d*", "Display the conglomerate info"),
    ConglomerateTileReach("^G\\d+ r", "Display the territory accessible to the conglomerate"),
    ConglomerateProduced("^G\\d+ p", "Display the resources produced be the conglomerate"),
    GroupRelations("^G\\d+ G\\d+", "Display relations between two conglomerates"),
    ConglomeratePotentials("^G\\d+ p \\d+", "Display tile potentials for the conglomerate"),
    MeaningfulResources("meaning", "Display resources with meaning on the map"),
    ArtificialResources("artificial", "Display artificial resources on the map"),
    GroupStatistics("gstat", "Display statistics across all groups"),
    Aspects("a \\w+", "Display aspect description"),
    CultureAspects("ca \\w+", "Display culture aspect description"),
    Strata("s \\w+", "Display Group with a strata containing the given substring on the map"),
    AddAspect("^G\\d+ \\w+", "Try adding aspect to the conglomerate"),
    AddWant("^want G\\d+ \\w+", "Try adding want to the conglomerate");

    override val pattern = Regex(command)
}

fun registerCultureCommands(commandManager: CommandManager<out TextCultureVisualizer>, handler: CommandExecutor<TextCultureVisualizer>) {
    commandManager.registerCommands(CultureCommand.values().toList())
    commandManager.registerHandler(handler)
}
