package io.tashtabash.sim.space.resource.instantiation


fun String.bracketSensitiveSplit(separator: Char, bracketLeft: Char = '(', bracketRight: Char = ')'): List<String> {
    val splitString = mutableListOf<String>()
    var start = 0
    var finish = 1
    var depth = 0

    while (finish <= this.length) {
        when (this[finish - 1]) {
            separator -> if (depth == 0) {
                splitString.add(this.substring(start, finish - 1))
                start = finish
            }
            bracketLeft -> depth++
            bracketRight -> {
                depth--
                if (depth < 0)
                    throw ParseException("Wrong bracket sequence - $this")
            }
        }
        finish++
    }
    splitString.add(this.substring(start, finish - 1))

    return splitString
}
