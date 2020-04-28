package simulation.culture.group.centers

import shmp.random.randomElement
import simulation.Controller
import simulation.Controller.*
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack
import simulation.space.resource.tag.labeler.ResourceLabeler
import simulation.space.tile.Tile

class ResourceCenter(private val cherishedResources: MutableResourcePack, private var storageTile: Tile) {
    val pack : ResourcePack
        get() = cherishedResources

    private val _direBound = 50

    private val neededResourcesMap = mutableMapOf<ResourceLabeler, ResourceNeed>()

    val neededResources: Map<ResourceLabeler, ResourceNeed>
        get() = neededResourcesMap.toMap()

    val mostImportantNeed: Pair<ResourceLabeler, ResourceNeed>?
        get() {
            neededResources.forEach { it.value.normalize() }
            val max = neededResources.entries
                    .maxBy { it.value.importance }
                    ?.value?.importance
                    ?: return null
            return randomElement(neededResources.filter { it.value.importance == max }.toList(), session.random)
        }

    val direNeed: Pair<ResourceLabeler, ResourceNeed>?
        get() {
            val result = mostImportantNeed
            return if (result != null && result.second.importance >= _direBound) result
            else null
        }

    private val _resourcesToAdd = mutableListOf<Resource>()

    fun takeResource(resource: Resource, amount: Int) = cherishedResources.getResourcePartAndRemove(resource, amount)

    fun die(disbandTile: Tile) {
        cherishedResources.disbandOnTile(disbandTile)
    }

    fun add(resource: Resource) {//TODO add resources on tile
        if (!cherishedResources.add(resource))
            _resourcesToAdd.add(resource)
    }

    fun addAll(resources: Collection<Resource>) {
        resources.forEach {cherishedResources.add(it)}
    }

    fun addAll(pack: ResourcePack) {
        pack.resources.forEach {cherishedResources.add(it)}
    }

    fun moveToNewStorage(newStorageTile: Tile) {
        if (newStorageTile == storageTile) return
        val staticResources = cherishedResources.getResources { !it.genome.isMovable }
        cherishedResources.removeAll(staticResources)
        storageTile.addDelayedResources(staticResources.resources)
        storageTile = newStorageTile
    }

    fun addNeeded(resourceLabeler: ResourceLabeler, importance: Int = 1) {
        if (neededResourcesMap.containsKey(resourceLabeler))
            neededResourcesMap.getValue(resourceLabeler).importance += importance
        else
            neededResourcesMap[resourceLabeler] = ResourceNeed(importance, true)
    }

    fun hasDireNeed() = neededResourcesMap.any { it.value.importance >= _direBound }

    fun finishUpdate() {
        _resourcesToAdd.clear()
        neededResourcesMap.values.forEach(ResourceNeed::finishUpdate)
        neededResourcesMap.entries.removeIf { it.value.importance <= 0 }
    }

    override fun toString(): String {
        return "Current resources:\n$cherishedResources\n" +
                "Needed resources: \n${printedNeeds()}\n\n"
    }

    private fun printedNeeds() = neededResources.entries.joinToString("\n")
    { "${it.key} - importance ${it.value.importance}" }
}

data class ResourceNeed(var importance: Int, var wasUpdated: Boolean = false) {
    fun normalize() {
        if (importance > 100) importance = 100
    }

    fun finishUpdate() {
        normalize()
        if (!wasUpdated) importance /= 2
        else wasUpdated = false
    }
}