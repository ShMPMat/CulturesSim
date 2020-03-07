package simulation.space.resource

import extra.InputDatabase
import simulation.culture.aspect.Aspect
import java.util.*

fun createPool(): ResourcePool {
    val resourceIdeals = ArrayList<ResourceIdeal>()
    val inputDatabase = InputDatabase("SupplementFiles/Resources")
    var line: String?
    var tags: Array<String>
    while (true) {
        line = inputDatabase.readLine() ?: break
        tags = line.split("\\s+".toRegex()).toTypedArray()
        resourceIdeals.add(createResource(tags))
    }
    val pool = ResourcePool(resourceIdeals)
    resourceIdeals.map { it.resourceCore }.forEach { it.actualizeLinks(pool) }
    resourceIdeals.map { it.resourceCore }.forEach { it.actualizeParts(pool) }
    return pool
}

private fun createResource(tags: Array<String>): ResourceIdeal {
    val resourceCore = ResourceCore(tags)
    return ResourceIdeal(resourceCore)
}

data class ResourceTemplate(
        val resourceIdeal: ResourceIdeal,
        val aspectConversion: Map<Aspect, Array<String>>,
        val parts: List<String>
)