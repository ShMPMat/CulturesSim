package shmp.visualizer.command


interface TextCommand : Command {
    val pattern: Regex
}
