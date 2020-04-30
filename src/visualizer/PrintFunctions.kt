package visualizer

import extra.OutputFunc
import simulation.World
import simulation.culture.aspect.Aspect
import simulation.culture.group.GroupConglomerate
import simulation.space.resource.Genome
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePool

fun resourcesCounter(world: World): String {
    val resourceAmounts = world.resourcePool.getAll()
            .filter { it.genome.type in listOf(Genome.Type.Animal, Genome.Type.Plant) }
            .map { it to ResourceCount() }
            .toMap()
    world.map.getTiles().forEach { t ->
        t.resourcePack.resources.forEach {
            if (resourceAmounts.containsKey(it))
                resourceAmounts.getValue(it).add(it)
        }
    }
    return resourceAmounts.entries.joinToString("\n", postfix = "\u001b[30m")
    {
        (if (it.value.amount == 0) "\u001b[31m" else "\u001b[30m") +
                "${it.key.fullName}: tiles - ${it.value.tilesAmount}, amount - ${it.value.amount}"
    }
}

data class ResourceCount(var amount: Int = 0, var tilesAmount: Int = 0) {
    fun add(resource: Resource) {
        amount += resource.amount
        tilesAmount++
    }
}

fun printProduced(group: GroupConglomerate) = group.subgroups
        .flatMap { it.cultureCenter.aspectCenter.aspectPool.converseWrappers }
        .flatMap { it.producedResources }
        .distinctBy { it.fullName }
        .sortedBy { it.fullName }
        .joinToString { it.fullName }

fun printApplicableResources(aspect: Aspect, resources: Collection<Resource>) = resources
        .filter { it.aspectConversion.containsKey(aspect) }
        .joinToString { it.fullName }
