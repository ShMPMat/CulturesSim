package simulation.culture.group.centers

import shmp.random.randomElement
import simulation.Controller.*
import simulation.culture.group.Place
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.ResourcePack
import simulation.space.resource.tag.labeler.ResourceLabeler
import simulation.space.tile.Tile
import simulation.space.tile.TileTag

class ResourceCenter(
        cherishedResources: MutableResourcePack,
        private var storageTile: Tile,
        private val groupName: String
) {
    private var movedAmount = 0
    private val tileTag: TileTag
        get() = TileTag(groupName + "_storage" + movedAmount, "storage")
    private var place = Place(storageTile, tileTag)
    val pack: ResourcePack
        get() = place.owned

    init {
        place.addResources(cherishedResources)
    }

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

    fun takeResource(resource: Resource, amount: Int) = place.takeResource(resource, amount)

    fun die() {
        session.world.strayPlacesManager.addPlace(place)
    }

    fun add(resource: Resource) = place.addResource(resource)

    fun addAll(resources: Collection<Resource>) = resources.forEach { add(it) }

    fun addAll(pack: ResourcePack) = addAll(pack.resources)

    fun moveToNewStorage(newStorageTile: Tile) {
        if (newStorageTile == storageTile) return
//        if (pack.any { !it.genome.isMovable }) {
//            val k = 0
//        }
        val oldPlace = place
        movedAmount++
        place = Place(newStorageTile, tileTag)
        val movableResources = oldPlace.getResourcesAndRemove { it.genome.isMovable }
        place.addResources(movableResources)
        storageTile = newStorageTile
        session.world.strayPlacesManager.addPlace(oldPlace)
    }

    fun addNeeded(resourceLabeler: ResourceLabeler, importance: Int = 1) {
        if (neededResourcesMap.containsKey(resourceLabeler))
            neededResourcesMap.getValue(resourceLabeler).importance += importance
        else
            neededResourcesMap[resourceLabeler] = ResourceNeed(importance, true)
    }

    fun hasDireNeed() = neededResourcesMap.any { it.value.importance >= _direBound }

    fun finishUpdate() {
        place.owned.resources.filter { it.isNotEmpty }.forEach {
            if (!place.tile.resourcesWithMoved.contains(it)) {
                val s = 0
            }
        }
        _resourcesToAdd.clear()
        neededResourcesMap.values.forEach(ResourceNeed::finishUpdate)
        neededResourcesMap.entries.removeIf { it.value.importance <= 0 }
    }

    override fun toString(): String {
        return "Current resources:\n${place.owned}\n" +
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