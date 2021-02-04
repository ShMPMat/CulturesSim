package shmp.utils

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
    val lineLength: Int = (left.lines().map { it.length }.maxOrNull() ?: 0) + 4
    val linesAmount = max(left.lines().count(), right.lines().count())
    val list1 = left.lines() + ((0 until linesAmount - left.lines().size).map { "" })
    val list2 = right.lines() + ((0 until linesAmount - right.lines().size).map { "" })

    for (i in 0 until linesAmount) {
        val ss1 = list1[i]
        val ss2 = list2[i]
        stringBuilder
                .append(ss1)
                .append(" ".repeat(if (guarantiedLength) 4 else lineLength - ss1.length))
                .append(ss2)
        if (i != linesAmount - 1)
            stringBuilder.append("\n")
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
    val prefix = text.lines().take(size).joinToString("\n")
    val postfix = text.lines().drop(size).joinToString("\n")

    if (postfix == "")
        return java.lang.StringBuilder(prefix)

    return addToRight(
            prefix,
            chompToLines(postfix, size).toString(),
            false
    )

}

fun chompToLines(text: StringBuilder, size: Int) = chompToLines(text.toString(), size)

fun getTruncated(t: Double, signs: Int) = 10.0.pow(signs).toInt().let { p ->
    (t * p).toInt().toDouble() / p
}.toString()

fun <E> E.addLinePrefix(prefix: String = "    ") = this.toString().lines()
        .joinToString("\n") { prefix + it }
