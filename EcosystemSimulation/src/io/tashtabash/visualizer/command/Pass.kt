package io.tashtabash.visualizer.command


object Pass : TextCommand {
    override val pattern = Regex("")
    override val description = "Do nothing"
}
