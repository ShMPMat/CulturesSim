package shmp.visualizer

import shmp.visualizer.command.CommandManager

interface Visualizer<E: Visualizer<E>> {
    fun print()

    val commandManager: CommandManager<E>
}