package simulation.space.generator

import simulation.space.resource.ResourceCore
import simulation.space.resource.ResourceIdeal

fun createResource(tags: Array<String>): ResourceIdeal {
    val resourceCore = ResourceCore(tags)
    return ResourceIdeal(resourceCore)
}