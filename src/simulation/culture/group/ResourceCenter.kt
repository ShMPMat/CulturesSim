package simulation.culture.group

import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack
import simulation.space.resource.tag.labeler.ResourceLabeler
import simulation.space.tile.Tile

class ResourceCenter(private val cherishedResources: MutableResourcePack, private var storageTile: Tile) {
    val resources
        get() = cherishedResources.resources

    private val neededResources = mutableMapOf<ResourceLabeler, ResourceNeed>()

    private val _resourcesToAdd = mutableListOf<Resource>()

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
        staticResources.resources.forEach { storageTile.addDelayedResource(it) }
        storageTile = newStorageTile
    }

    fun addNeeded(resourceLabeler: ResourceLabeler, importance: Int = 1) {
        if (neededResources.containsKey(resourceLabeler))
            neededResources.getValue(resourceLabeler).importance += importance
        else
            neededResources[resourceLabeler] = ResourceNeed(importance, true)
    }

    fun hasDireNeed() = neededResources.any { it.value.importance >= 50 }

    fun finishUpdate() {
        _resourcesToAdd.clear()
        neededResources.values.forEach(ResourceNeed::finishUpdate)
        neededResources.entries.removeIf { it.value.importance <= 0 }
    }

    override fun toString(): String {
        return "Current resources:\n$cherishedResources\n\n"
    }
}

private data class ResourceNeed(var importance: Int, var wasUpdated: Boolean = false) {
    fun finishUpdate() {
        if (importance > 100) importance = 100
        if (!wasUpdated) importance /= 2
        else wasUpdated = false
    }
}