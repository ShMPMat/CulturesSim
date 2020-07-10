package extra

import kotlin.math.max
import kotlin.math.pow

/**
 * Adds right text to the left text. right text will be lined up with
 * consideration to longest line from the left text.
 *
 * @param guarantiedLength if left fragment is already has equal length of lines.
 * @return left and right text merged in one.
 */
fun addToRight(left: String, right: String, guarantiedLength: Boolean): StringBuilder {
    if (left.isEmpty())
        return StringBuilder(right)

    val stringBuilder = StringBuilder()
    val m: Int = (left.lines().map { it.length }.max() ?: 0) + 4
    val list1 = left.lines()
    val list2 = right.lines()

    for (i in 0 until max(left.lines().count(), right.lines().count())) {
        val ss1 = if (i < list1.size)
            list1[i]
        else ""
        val ss2 = if (i < list2.size)
            list2[i]
        else ""
        val k = " "
        stringBuilder
                .append(ss1)
                .append(" ".repeat(if (guarantiedLength) 4 else m - ss1.length))
                .append(ss2).append("\n")
    }

    return stringBuilder
}

fun addToRight(left: StringBuilder, right: StringBuilder, guarantiedLength: Boolean) =
        addToRight(left.toString(), right.toString(), guarantiedLength)

/**
 * Edits text via carrying lines which are longer than size.
 * @param text text which will be edited.
 * @param size maximal acceptable length of a line.
 * @return edited text, each line in it is not longer than size. Words can be cut in the middle.
 */
fun chompToSize(text: String, size: Int): StringBuilder {
    val stringBuilder = StringBuilder()

    for (_line in text.lines()) {
        var line = _line
        while (line.isNotEmpty())
            if (size >= line.length) {
                stringBuilder.append(line)
                break
            } else {
                var part = line.substring(0, size)
                if (part.lastIndexOf(" ") != -1) {
                    line = line.substring(part.lastIndexOf(" ") + 1)
                    part = (if (part.lastIndexOf(" ") == 0)
                        part.substring(1)
                    else
                        part.substring(0, part.lastIndexOf(" ")))
                } else
                    line = line.substring(size)
                stringBuilder.append(part).append("\n")
            }
        stringBuilder.append("\n")
    }

    return stringBuilder
}

/**
 * Edits text via adding lines after size + 1 recursively to the right.
 * @param text text which will be modified.
 * @param size maximal acceptable number of lines.
 * @return edited text, has no more than size lines, greater lines added to the right.
 */
fun chompToLines(text: String, size: Int): StringBuilder {
    var index = 0
    var counter = 0

    while (index + 1 < text.length) {
        index = text.indexOf("\n", index + 1)
        counter++
        if (counter == size)
            return addToRight(
                    text.substring(0, index),
                    chompToLines(text.substring(index + 1), size).toString(),
                    false
            )
    }

    return StringBuilder(text)
}

fun chompToLines(text: StringBuilder, size: Int) = chompToLines(text.toString(), size)

fun getTruncated(t: Double, signs: Int) = 10.0.pow(signs).toInt().let { p ->
    (t * p).toInt().toDouble() / p
}.toString()

fun <E> E.addLinePrefix(prefix: String = "    ") = this.toString().lines()
        .joinToString("\n") { prefix + it }
