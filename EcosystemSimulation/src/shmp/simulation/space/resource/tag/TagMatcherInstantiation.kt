package shmp.simulation.space.resource.tag

import shmp.utils.InputDatabase
import shmp.simulation.space.resource.tag.labeler.makeResourceLabeler
import shmp.simulation.space.resource.tag.leveler.ConstLeveler
import shmp.simulation.space.resource.tag.leveler.ResourceLeveler
import shmp.simulation.space.resource.tag.leveler.makeResourceLeveler
import java.net.URL
import java.util.*


fun createTagMatchers(path: Enumeration<URL>): List<TagMatcher> {
    val matchers: MutableList<TagMatcher> = mutableListOf()
    val inputDatabase = InputDatabase(path)

    while (true) {
        val line = inputDatabase.readLine() ?: break
        val tags = line.split("\\s+".toRegex())
        var leveler: ResourceLeveler = ConstLeveler(1)

        var name = tags[0]
        if (name.contains(':')) {
            leveler = makeResourceLeveler(name.split(':')[1])
            name = name.split(':')[0]
        }

        matchers.add(TagMatcher(
                ResourceTag(name),
                makeResourceLabeler(tags.drop(1).joinToString(",")),
                leveler
        ))
    }
    return matchers
}
