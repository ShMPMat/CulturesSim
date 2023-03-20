package shmp.visualizer.command

import shmp.visualizer.Visualizer


class CommandManager<E : Visualizer>(private val defaultHandler: CommandExecutor<E>)  {
    private val commands: MutableList<TextCommand> = mutableListOf()

    var defaultCommand: TextCommand = Pass

    private val handlers: MutableList<CommandExecutor<E>> = mutableListOf()

    fun registerCommands(newCommands: List<TextCommand>) = commands.addAll(newCommands)

    fun registerHandler(newHandler: CommandExecutor<E>) = handlers.add(newHandler)

    private fun getCommand(line: String): TextCommand {
        for (command in commands)
            if (command.pattern.matches(line))
                return command
        return defaultCommand
    }

    fun <T: E> handleCommand(line: String, visualizer: T): ExecutionResult {
        val command = getCommand(line)

        for (handler in handlers) {
            val executionResult = handler.tryRun(line, command, visualizer)

            if (executionResult != ExecutionResult.NotFound)
                return executionResult
        }

        return defaultHandler.tryRun(line, defaultCommand, visualizer)
    }
}
