package shmp.utils

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.net.URL
import java.util.*


class InputDatabase(private val paths: List<String>) {
    private var readerIndex = 0
    private var bufferedReader = BufferedReader(FileReader(paths[readerIndex]))
    private var lastLine: String? = null

    init {
        lastLine = bufferedReader.readLine()
        while ((lastLine != null || nextReader()) && doSkipLine(lastLine!!))
            lastLine = bufferedReader.readLine()
    }

    constructor(path: String) : this(listOf(path))
    constructor(urls: Enumeration<URL>) : this(urls.toList().map { it.path })

    private fun doSkipLine(string: String) = string.isBlank() || string[0] == '/'

    private fun nextReader(): Boolean {
        readerIndex++

        if (readerIndex >= paths.size)
            return false

        bufferedReader = BufferedReader(FileReader(paths[readerIndex]))

        return true
    }

    fun readLine(): String? {
        lastLine?.let { wholeLine ->
            val line = StringBuilder(wholeLine.drop(1))

            while (true) {
                var newLine: String?
                newLine = try {
                    bufferedReader.readLine()
                } catch (e: IOException) {
                    System.err.println(e.toString())
                    return null
                }

                if (newLine == null)
                    if (nextReader())
                        continue
                    else {
                        lastLine = null
                        return line.toString()
                    }
                if (doSkipLine(newLine))
                    continue
                if (newLine[0] == '-') {
                    lastLine = newLine
                    return line.toString()
                }
                line.append(" ").append(newLine)
            }
        }

        return null
    }

    fun readLines(): List<String> {
        val lines: MutableList<String> = ArrayList()
        var line = readLine()
        while (line != null) {
            lines.add(line)
            line = readLine()
        }
        return lines
    }
}