package simulation.space.resource.tag

import extra.InputDatabase
import simulation.space.resource.tag.labeler.makeResourceLabeler

fun createTagMatchers(path: String): List<TagMatcher> {
    val matchers: MutableList<TagMatcher> = mutableListOf()
    val inputDatabase = InputDatabase(path)
    while (true) {
        val line = inputDatabase.readLine() ?: break
        val tags = line.split("\\s+".toRegex()).toTypedArray()
        matchers.add(TagMatcher(
                ResourceTag(tags[0]),
                makeResourceLabeler(tags.drop(1))
        ))
    }
    return matchers
    }
