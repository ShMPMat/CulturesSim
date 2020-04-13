package simulation.culture.group

import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack
import simulation.space.resource.tag.labeler.ResourceLabeler
import simulation.space.tile.Tile
import java.util.Comparator

class ResourceCenter(private val cherishedResources: MutableResourcePack, private var storageTile: Tile) {
    val resources
        get() = cherishedResources.resources

    private val DIRE_BOUND = 50

    private val neededResourcesMap = mutableMapOf<ResourceLabeler, ResourceNeed>()

    val neededResources: Map<ResourceLabeler, ResourceNeed>
        get() = neededResourcesMap.toMap()

    val mostImportantNeed: Pair<ResourceLabeler, ResourceNeed>?
        get() = neededResources.entries//TODO all maxes
                .maxBy { it.value.importance }
                ?.toPair()

    val direNeed: Pair<ResourceLabeler, ResourceNeed>?
        get() {
            val result = neededResources.entries
                    .maxBy { it.value.importance }
                    ?.toPair()
            return if (result != null && result.second.importance >= DIRE_BOUND) result
            else null
        }

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
        if (neededResourcesMap.containsKey(resourceLabeler))
            neededResourcesMap.getValue(resourceLabeler).importance += importance
        else
            neededResourcesMap[resourceLabeler] = ResourceNeed(importance, true)
    }

    fun hasDireNeed() = neededResourcesMap.any { it.value.importance >= DIRE_BOUND }

    fun finishUpdate() {
        _resourcesToAdd.clear()
        neededResourcesMap.values.forEach(ResourceNeed::finishUpdate)
        neededResourcesMap.entries.removeIf { it.value.importance <= 0 }
    }

    override fun toString(): String {
        return "Current resources:\n$cherishedResources\n\n"
    }
}

data class ResourceNeed(var importance: Int, var wasUpdated: Boolean = false) {
    fun finishUpdate() {
        if (importance > 100) importance = 100
        if (!wasUpdated) importance /= 2
        else wasUpdated = false
    }
}