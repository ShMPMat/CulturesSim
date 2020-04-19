package visualizer

import simulation.World
import simulation.space.resource.Genome
import simulation.space.resource.Resource

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
    return resourceAmounts.entries.joinToString(separator = "\n", postfix = "\u001b[30m")
    { (if (it.value.amount == 0) "\u001b[31m" else "\u001b[30m") +
            "${it.key.fullName}: tiles - ${it.value.tilesAmount}, amount - ${it.value.amount}" }
}

data class ResourceCount(var amount: Int = 0, var tilesAmount: Int = 0) {
    fun add(resource: Resource) {
        amount += resource.amount
        tilesAmount++
    }
}