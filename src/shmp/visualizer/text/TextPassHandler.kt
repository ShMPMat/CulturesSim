package shmp.visualizer.text

import shmp.visualizer.command.Command
import shmp.visualizer.command.CommandHandler

object TextPassHandler: CommandHandler<TextVisualizer> {
    override fun tryRun(line: String, command: Command, visualizer: TextVisualizer): Boolean {
        println("No action taken")
        return true
    }
}