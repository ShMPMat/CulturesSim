package simulation.culture.group

import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack
import simulation.space.tile.Tile

class ResourceCenter(private val cherishedResources: MutableResourcePack, private var storageTile: Tile) {
    val resources
        get() = cherishedResources.resources

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

    fun finishUpdate() {
        _resourcesToAdd.clear()
    }

    override fun toString(): String {
        return "Current resources:\n$cherishedResources\n\n"
    }
}