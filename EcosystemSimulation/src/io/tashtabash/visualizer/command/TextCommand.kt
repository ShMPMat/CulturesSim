package io.tashtabash.visualizer.command


interface TextCommand : Command {
    val pattern: Regex
}
