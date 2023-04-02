package io.tashtabash.simulation.culture.group.centers

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.simulation.CulturesController.Companion.session
import io.tashtabash.simulation.SimulationError
import io.tashtabash.simulation.culture.group.cultureaspect.CultureAspect
import io.tashtabash.simulation.culture.group.place.MovablePlace
import io.tashtabash.simulation.culture.group.request.RequestType
import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.Taker
import io.tashtabash.simulation.space.resource.container.MutableResourcePack
import io.tashtabash.simulation.space.resource.container.ResourcePack
import io.tashtabash.simulation.space.resource.tag.labeler.ResourceLabeler
import io.tashtabash.simulation.space.tile.Tile
import io.tashtabash.simulation.space.tile.TileTag
import io.tashtabash.utils.addLinePrefix


class ResourceCenter(cherishedResources: MutableResourcePack, storageTile: Tile, groupName: String) {
    private var place = MovablePlace(storageTile, TileTag(groupName + "_storage", "storage"))
    val pack: ResourcePack
        get() = place.current.owned

    init {
        place.current.addResources(cherishedResources)
    }

    private val _direBound = 50

    private val neededResourcesMap = mutableMapOf<ResourceLabeler, ResourceNeed>()

    private val _bannedResources = mutableMapOf<Resource, ResourceBan>()
    val bannedResources: Map<Resource, ResourceBan> = _bannedResources

    val neededResources: Map<ResourceLabeler, ResourceNeed>
        get() = neededResourcesMap.toMap()

    val mostImportantNeed: Pair<ResourceLabeler, ResourceNeed>?
        get() {
            neededResources.forEach { it.value.normalize() }

            val max = neededResources.entries
                    .maxByOrNull { it.value.importance }
                    ?.value?.importance
                    ?: return null

            return neededResources.filter { it.value.importance == max }.toList().randomElement()
        }

    val direNeed: Pair<ResourceLabeler, ResourceNeed>?
        get() {
            val result = mostImportantNeed
            return if (result != null && result.second.importance >= _direBound) result
            else null
        }

    private val _resourcesToAdd = mutableListOf<Resource>()

    fun takeResource(resource: Resource, amount: Int, taker: Taker) =
            place.current.takeResource(resource, amount, taker)

    fun getResource(resource: Resource) = place.current.getResource(resource)

    fun die() {
        session.world.strayPlacesManager.addPlace(place.current)
    }

    fun add(resource: Resource) = place.current.addResource(resource)

    fun addAll(resources: Collection<Resource>) = resources.forEach { add(it) }

    fun addAll(pack: ResourcePack) = addAll(pack.resources)

    fun moveToNewStorage(newStorageTile: Tile) = place.move(newStorageTile)

    fun addNeeded(resourceLabeler: ResourceLabeler, importance: Int = 1) {
        if (neededResourcesMap.containsKey(resourceLabeler))
            neededResourcesMap.getValue(resourceLabeler).importance += importance
        else
            neededResourcesMap[resourceLabeler] = ResourceNeed(importance, true)
    }

    fun hasDireNeed() = neededResourcesMap.any { it.value.importance >= _direBound }

    fun finishUpdate() {
        place.current.owned.resources.filter { it.isNotEmpty }.forEach {
            if (!place.current.tile.resourcesWithMoved.contains(it)) {
                val s = 0//TODO remove it if so
            }
        }
        _resourcesToAdd.clear()
        neededResourcesMap.values.forEach(ResourceNeed::finishUpdate)
        neededResourcesMap.entries.removeIf { it.value.importance <= 0 }

        if (session.isTime(500))
            pack.clearEmpty()
    }

    private fun printedNeeds() = neededResources.entries.joinToString("\n") {
        "${it.key} - importance ${it.value.importance}"
    }

    fun needLevel(resource: Resource) = neededResources
            .filter { it.key.isSuitable(resource.genome) }
            .map { it.value.importance }
            .maxOrNull()
            ?: 0

    fun addBan(resource: Resource, provider: ResourceBanProvider) {
        val existingBan = _bannedResources[resource]

        if (existingBan == null) {
            _bannedResources[resource] = ResourceBan(provider.allowedTypes.toMutableSet(), mutableListOf(provider))
        } else {
            existingBan.providers.add(provider)
            existingBan.allowedTypes.removeIf { it in provider.allowedTypes }
        }
    }

    fun removeBan(resource: Resource, provider: ResourceBanProvider) {
        val existingBan = _bannedResources[resource]

        if (existingBan != null) {
            val providers = existingBan.providers
            providers.remove(provider)

            _bannedResources.remove(resource)

            providers.forEach { addBan(resource, it) }
        } else
            throw SimulationError("Trying to remove non-existing ban on ${resource.fullName}")
    }

    override fun toString() = """
        |Current resources:
        |${place.current.owned.addLinePrefix()}
        |Needed resources: 
        |${printedNeeds().addLinePrefix()}
        |Banned resources:
        |${bannedResources.entries.joinToString("\n") { (r, b) -> r.fullName + " " + b }}
        """.trimMargin()
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

data class ResourceBan(val allowedTypes: MutableSet<RequestType>, val providers: MutableList<ResourceBanProvider>) {
    override fun toString() = if (allowedTypes.isEmpty()) "" else " only for " + allowedTypes.joinToString()
}

interface ResourceBanProvider {
    val allowedTypes: Set<RequestType>
}
