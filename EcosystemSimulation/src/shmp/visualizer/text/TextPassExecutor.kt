package shmp.visualizer.text

import shmp.visualizer.Visualizer
import shmp.visualizer.command.Command
import shmp.visualizer.command.CommandExecutor


class TextPassExecutor<E: Visualizer>: CommandExecutor<E> {
    override fun tryRun(line: String, command: Command, visualizer: E): Boolean {
        println("No action taken")
        return true
    }
}
