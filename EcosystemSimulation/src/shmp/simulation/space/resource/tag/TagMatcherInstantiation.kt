package shmp.simulation.space.resource.tag

import shmp.utils.InputDatabase
import shmp.simulation.space.resource.tag.labeler.makeResourceLabeler
import java.net.URL
import java.util.*


fun createTagMatchers(path: Enumeration<URL>): List<TagMatcher> {
    val matchers: MutableList<TagMatcher> = mutableListOf()
    val inputDatabase = InputDatabase(path)

    while (true) {
        val line = inputDatabase.readLine() ?: break
        val tags = line.split("\\s+".toRegex())

        var name = tags[0]
        var level = 1
        if (name.contains(':')) {
            level = name.split(':')[1].toInt()
            name = name.split(':')[0]
        }

        matchers.add(TagMatcher(
                ResourceTag(name, level),
                makeResourceLabeler(tags.drop(1).joinToString(","))
        ))
    }
    return matchers
}
