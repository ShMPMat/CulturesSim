package io.tashtabash.sim.init

import io.tashtabash.sim.EcosystemWorld
import io.tashtabash.sim.space.resource.action.ActionTag
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.createTagMatchers
import io.tashtabash.utils.InputDatabase
import java.util.Collections


fun constructWorld(): EcosystemWorld {
    val classLoader = Thread.currentThread().contextClassLoader
    val tagMatchers = createTagMatchers(classLoader.getResources("ResourceTagLabelers"))
    val tags = InputDatabase(
        Collections.enumeration(
            getResourcePaths(
                classLoader.getResources("ResourceTags/").toList()
            )
        )
    )
        .readLines()
        .map { ResourceTag(it) }
        .union(tagMatchers.map { it.tag })
    val actionTags = InputDatabase(classLoader.getResources("ActionTags"))
        .readLines()
        .map { ActionTag(it) }

    return EcosystemWorld(tagMatchers, tags, actionTags)
}
