package io.tashtabash.sim.space.resource.tag

import io.tashtabash.utils.InputDatabase
import io.tashtabash.sim.space.resource.tag.labeler.makeResourceLabeler
import io.tashtabash.sim.space.resource.tag.leveler.ConstLeveler
import io.tashtabash.sim.space.resource.tag.leveler.ResourceLeveler
import io.tashtabash.sim.space.resource.tag.leveler.makeResourceLeveler
import java.net.URL
import java.util.*


fun createTagMatchers(path: Enumeration<URL>): List<TagMatcher> {
    val matchers: MutableList<TagMatcher> = mutableListOf()
    val inputDatabase = InputDatabase(path)

    while (true) {
        val line = inputDatabase.readLine()
            ?: break
        val tags = line.split("\\s+".toRegex())
        var leveler: ResourceLeveler = ConstLeveler(1.0)
        var wipeOnMismatch = false

        var name = tags[0]
        if (name.contains(':')) {
            leveler = makeResourceLeveler(name.split(':')[1])
            name = name.split(':')[0]
        }
        if (name[0] == '!') {
            name = name.substring(1)
            wipeOnMismatch = true
        }

        matchers += TagMatcher(
            ResourceTag(name),
            makeResourceLabeler(tags.drop(1).joinToString(",")),
            leveler,
            wipeOnMismatch
        )
    }
    return matchers
}
