package io.tashtabash.visualizer.command

import io.tashtabash.visualizer.Visualizer


interface CommandExecutor<in E : Visualizer> {
    fun tryRun(line: String, command: Command, visualizer: E): ExecutionResult
}
