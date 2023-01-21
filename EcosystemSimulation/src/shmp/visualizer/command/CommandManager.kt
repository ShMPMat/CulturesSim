package shmp.visualizer.command

import shmp.visualizer.Visualizer


class CommandManager<E : Visualizer>(private val defaultHandler: CommandExecutor<E>)  {
    private val commands: MutableList<Command> = mutableListOf()

    var defaultCommand: Command = Pass

    private val handlers: MutableList<CommandExecutor<E>> = mutableListOf()

    fun registerCommands(newCommands: List<Command>) = commands.addAll(newCommands)

    fun registerHandler(newHandler: CommandExecutor<E>) = handlers.add(newHandler)

    private fun getCommand(line: String): Command {
        for (command in commands)
            if (command.pattern.matches(line)) return command
        return defaultCommand
    }

    fun <T: E> handleCommand(line: String, visualizer: T) {
        val command = getCommand(line)
        for (handler in handlers)
            if (handler.tryRun(line, command, visualizer))
                return
        defaultHandler.tryRun(line, defaultCommand, visualizer)
    }
}
