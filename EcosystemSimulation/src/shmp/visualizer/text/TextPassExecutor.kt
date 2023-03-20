package shmp.visualizer.text

import shmp.visualizer.Visualizer
import shmp.visualizer.command.Command
import shmp.visualizer.command.CommandExecutor
import shmp.visualizer.command.ExecutionResult


class TextPassExecutor<E: Visualizer>: CommandExecutor<E> {
    override fun tryRun(line: String, command: Command, visualizer: E): ExecutionResult {
        println("No action taken")

        return ExecutionResult.Success
    }
}
