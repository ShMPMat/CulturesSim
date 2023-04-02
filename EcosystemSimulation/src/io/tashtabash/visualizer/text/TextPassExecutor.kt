package io.tashtabash.visualizer.text

import io.tashtabash.visualizer.Visualizer
import io.tashtabash.visualizer.command.Command
import io.tashtabash.visualizer.command.CommandExecutor
import io.tashtabash.visualizer.command.ExecutionResult


class TextPassExecutor<E: Visualizer>: CommandExecutor<E> {
    override fun tryRun(line: String, command: Command, visualizer: E): ExecutionResult {
        println("No action taken")

        return ExecutionResult.Success
    }
}
