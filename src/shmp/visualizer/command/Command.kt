package shmp.visualizer.command

import shmp.visualizer.TextVisualizer
import shmp.visualizer.Visualizer


//Commands which can be given to shmp.visualizer.

interface Command {
    val pattern: Regex
}

object Pass : Command {
    override val pattern = Regex("")
}


interface CommandExecutor {
    fun Pair.tryRun()
}


object CommandManager {
    private var commands: MutableList<Command> = mutableListOf()

    var defaultCommand: Command = Pass

    fun registerCommands(newCommands: List<Command>) = commands.addAll(newCommands)

    fun getCommand(line: String): Command {
        for (command in commands)
            if (command.pattern.matches(line)) return command
        return defaultCommand
    }
}
