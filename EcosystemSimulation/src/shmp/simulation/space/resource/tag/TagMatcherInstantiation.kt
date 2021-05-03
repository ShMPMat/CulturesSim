package shmp.simulation.space.resource.tag

import shmp.utils.InputDatabase
import shmp.simulation.space.resource.tag.labeler.makeResourceLabeler


fun createTagMatchers(path: String): List<TagMatcher> {
    val matchers: MutableList<TagMatcher> = mutableListOf()
    val inputDatabase = InputDatabase(path)
    while (true) {
        val line = inputDatabase.readLine() ?: break
        val tags = line.split("\\s+".toRegex())
        matchers.add(TagMatcher(
                ResourceTag(tags[0]),
                makeResourceLabeler(tags.drop(1).joinToString(","))
        ))
    }
    return matchers
}
