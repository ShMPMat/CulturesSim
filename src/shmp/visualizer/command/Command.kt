package shmp.visualizer.command

import shmp.visualizer.Visualizer


interface Command {
    val pattern: Regex
}

interface CommandHandler<E : Visualizer<E>> {
    fun tryRun(line: String, command: Command, visualizer: E): Boolean
}


object Pass : Command {
    override val pattern = Regex("")
}


class CommandManager<E : Visualizer<E>>(val defaultHandler: CommandHandler<E>)  {
    private val commands: MutableList<Command> = mutableListOf()
    var defaultCommand: Command = Pass

    private val handlers: MutableList<CommandHandler<E>> = mutableListOf()

    fun registerCommands(newCommands: List<Command>) = commands.addAll(newCommands)
    fun registerHandler(newHandler: CommandHandler<E>) = handlers.add(newHandler)

    private fun getCommand(line: String): Command {
        for (command in commands)
            if (command.pattern.matches(line)) return command
        return defaultCommand
    }

    fun handleCommand(line: String, visualizer: E) {
        val command = getCommand(line)
        for (handler in handlers)
            if (handler.tryRun(line, command, visualizer))
                return
        defaultHandler.tryRun(line, defaultCommand, visualizer)
    }
}