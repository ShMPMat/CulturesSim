package shmp.visualizer.text

import shmp.visualizer.command.Command
import shmp.visualizer.command.CommandHandler

object TextPassHandler: CommandHandler<TextEcosystemVisualizer> {
    override fun tryRun(line: String, command: Command, visualizer: TextEcosystemVisualizer): Boolean {
        println("No action taken")
        return true
    }
}