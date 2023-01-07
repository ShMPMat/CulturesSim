package shmp.visualizer.command

import shmp.visualizer.Visualizer


interface CommandHandler<in E : Visualizer> {
    fun tryRun(line: String, command: Command, visualizer: E): Boolean
}
