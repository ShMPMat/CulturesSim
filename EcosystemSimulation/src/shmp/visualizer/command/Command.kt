package shmp.visualizer.command


interface Command {
    val pattern: Regex
    val description: String
}
