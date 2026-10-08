package io.tashtabash.sim.space.tile

import io.tashtabash.sim.DataInitializationError
import io.tashtabash.sim.space.Scale
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.TectonicPlate
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.resource.container.ResourcePack
import io.tashtabash.sim.space.resource.dependency.LabelerDependency
import io.tashtabash.sim.space.resource.dependency.NOT_DEPENDENCY
import io.tashtabash.sim.space.tile.updater.TileUpdater
import java.util.*
import kotlin.math.max


class Tile(
    val id: Int,
    val x: Int,
    val y: Int,
    val updaters: MutableList<TileUpdater> = mutableListOf(),
    val scale: Scale,
    val area: Double = scale.tileAreaKm2
) {
    val tagPool = MutableTileTagPool()

    var type: Type = Type.Water // It's Water in order for init {..} to set Normal correctly
        internal set

    var plate: TectonicPlate? = null

    private val _resourcePack = MutableResourcePack()
    val resourcePack: ResourcePack
        get() = _resourcePack

    //Resources added on this Tile on the last turn. They are stored here before the end of the turn.
    private val _delayedResources: MutableList<Resource> = ArrayList()

    val resourceDensity
        get() = resourcePack.resources.sumOf { it.amount * it.genome.volume } / (data.resourceCapacityPerKm2 * area)

    var level = 0
        internal set

    //Lowest level of this Tile which corresponds to the ground level.
    var secondLevel = 0
        private set

    var temperature = 0.0
        private set

    var neighbours = listOf<Tile>()
        private set

    @PublishedApi // In the order of neighbours
    internal var neighbourDirections = arrayOf<Direction>()
        private set

    fun setNeighbours(tiles: List<Pair<Tile, Direction>>) {
        if (neighbours.isNotEmpty())
            throw DataInitializationError("Neighbours are already set")

        neighbours = tiles.map { it.first }
        neighbourDirections = tiles.map { it.second }.toTypedArray()
    }

    // null if the Tile isn't a neighbour
    fun directionOf(tile: Tile): Direction? {
        val i = neighbours.indexOf(tile)
        return if (i != -1)
            neighbourDirections[i]
        else null
    }

    inline fun forEachNeighbourIn(direction: Direction, action: (Tile) -> Unit) {
        for (i in neighbours.indices)
            if (neighbourDirections[i] == direction)
                action(neighbours[i])
    }

    inline fun anyNeighbourIn(direction: Direction, predicate: (Tile) -> Boolean): Boolean {
        for (i in neighbours.indices)
            if (neighbourDirections[i] == direction && predicate(neighbours[i]))
                return true

        return false
    }

    fun countNeighboursIn(direction: Direction) = neighbourDirections.count { it == direction }

    init {
        updateTemperature()
        setType(Type.Normal, true)
    }

    private val windCenter = WindCenter()

    val wind: Wind
        get() = windCenter.wind

    val flow = Flow()

    val resourcesWithMoved: List<Resource>
        get() {
            val resources: MutableList<Resource> = ArrayList(_resourcePack.resources)
            resources.addAll(_delayedResources)
            return resources
        }

    fun getAccessibleResources(radius: Int = 1): List<Iterator<Resource>> {
        val accessibleResources = listOf(_resourcePack.resourcesIterator)
        val neighbours = getTilesInRadius(radius)

        return accessibleResources + neighbours.map { it._resourcePack.resourcesIterator }
    }

    inline fun forEachAccessibleResource(radius: Int = 1, action: (Resource) -> Boolean): Boolean {
        for (res in resourcePack.resourcesIterator)
            if (action(res))
                return true

        for (neighbour in getTilesInRadius(radius))
            for (res in neighbour.resourcePack.resourcesIterator)
                if (action(res))
                    return true

        return false
    }

    private val dependencyMatches = IdentityHashMap<LabelerDependency, DependencyMatches>()

    fun getDependencyMatches(dependency: LabelerDependency): DependencyMatches {
        val keysVersion = _resourcePack.keysVersion
        dependencyMatches[dependency]?.let {
            if (it.keysVersion == keysVersion)
                return it
        }

        // No relevant cache found, [re]build
        val matches = DependencyMatches(keysVersion)
        for (res in _resourcePack.resourcesIterator) {
            val worth = dependency.oneResourceWorth(res)
            if (worth != NOT_DEPENDENCY)
                matches.add(res, worth)
        }

        return matches.also { dependencyMatches[dependency] = it }
    }

    inline fun forEachAccessibleMatch(
        dependency: LabelerDependency,
        radius: Int = 1,
        action: (Resource, Int) -> Boolean
    ): Boolean {
        if (getDependencyMatches(dependency).any(action))
            return true

        for (neighbour in getTilesInRadius(radius))
            if (neighbour.getDependencyMatches(dependency).any(action))
                return true

        return false
    }

    fun getNeighbours(predicate: (Tile) -> Boolean) = neighbours.filter(predicate)

    private val radiusCache = arrayOfNulls<List<Tile>>(10).also {
        it[0] = listOf()
    }

    fun getTilesInRadius(radius: Int): List<Tile> {
        radiusCache.getOrNull(radius)
            ?.let { return it }

        val tiles = neighbours.toMutableSet()
        var outer = neighbours.toSet()

        repeat(radius - 1) {
            outer = outer.flatMap { it.neighbours }
                .filter { !tiles.contains(it) }
                .toSet()
            tiles += outer
        }

        tiles -= this

        return tiles.toList().also {
            // Large radii are rare and their lists are big, so they aren't cached
            if (radius < radiusCache.size)
                radiusCache[radius] = it
        }
    }

    fun getTilesInRadius(radius: Int, predicate: (Tile) -> Boolean) = getTilesInRadius(radius)
        .filter(predicate)
        .toSet()

    // Sets the terrain as it is, e.g. one derived from finer Tiles
    internal fun setTerrain(type: Type, level: Int, secondLevel: Int) {
        this.type = type
        this.level = level
        this.secondLevel = secondLevel
        updateTemperature()
    }

    fun setType(type: Type, updateLevel: Boolean) {
        if (type == this.type)
            return

        this.type = type

        if (!updateLevel)
            return

        when (type) {
            Type.Mountain -> {
                level = MOUNTAIN_LEVEL
                secondLevel = MOUNTAIN_LEVEL
            }
            Type.Normal -> {
                level = 100
                secondLevel = 100
            }
            Type.Water -> {
                level = data.seabedLevel
                secondLevel = data.seabedLevel
            }
            Type.Ice -> {
                level = data.defaultWaterLevel
            }
            else -> {}
        }
    }

    fun setLevel(newLevel: Int) {
        val wasWater = type == Type.Water
        level = newLevel
        secondLevel = newLevel

        type = when {
            newLevel < data.defaultWaterLevel -> Type.Water
            newLevel >= MOUNTAIN_LEVEL -> Type.Mountain
            else -> Type.Normal
        }

        if (type == Type.Water && !wasWater)
            addDelayedResource(data.resourcePool.getBaseName("SaltWater"))
    }

    private fun addResource(resource: Resource) {
        val oldKeysVersion = _resourcePack.keysVersion
        _resourcePack.add(resource)
        val keysVersion = _resourcePack.keysVersion
        if (keysVersion == oldKeysVersion)
            return

        // Try to cache the new Resource instead of rebuilding DependencyMatches later
        for ((dependency, matches) in dependencyMatches) {
            if (matches.keysVersion != oldKeysVersion)
                continue

            val worth = dependency.oneResourceWorth(resource)
            if (worth != NOT_DEPENDENCY)
                matches.add(resource, worth)
            matches.keysVersion = keysVersion
        }
    }

     // Adds resources which will be available on this Tile on the next turn.
    fun addDelayedResource(resource: Resource) {
        if (resource.isEmpty)
            return

        _delayedResources += resource
    }

    fun addDelayedResources(resources: Collection<Resource>) = resources.forEach { addDelayedResource(it) }

    fun addDelayedResources(pack: ResourcePack) = addDelayedResources(pack.resources)

    fun removeResource(resource: Resource) {
        _resourcePack.remove(resource)
        _delayedResources.remove(resource)
    }

    fun startUpdate() {
        resourcePack.resources.forEach { it.takers.clear() }

        updateResources()
        windCenter.useWind(_resourcePack.resources)
        updateTemperature()

        for (updater in updaters)
            updater.update(this)
    }

    fun middleUpdate() {
        _delayedResources.forEach { addResource(it) }
        _delayedResources.clear()
        windCenter.middleUpdate(this)
    }

    fun finishUpdate() {
        windCenter.finishUpdate()

    }

    private fun updateTemperature() {
        val start = data.temperatureBaseStart
        val finish = data.temperatureBaseFinish
        val scaledSize = data.worldSizeXKm / scale.tileSizeKm
        var levelShift = 0
        if (type == Type.Water || type == Type.Ice) {
            levelShift -= 2
            val deepness = max(data.defaultWaterLevel - secondLevel, 0)
            levelShift -= deepness * 5 / (data.defaultWaterLevel - data.seabedLevel) // max -5
        } else {
            val elevation = max(level - data.defaultWaterLevel, 0)
            levelShift -= elevation / 3
        }
        temperature = start + x * (finish - start) / scaledSize + levelShift
    }

    private fun updateResources() {
        val deletedResources = mutableListOf<Resource>()

        for (resource in _resourcePack.resources) {
            val result = resource.update(this)

            if (!result.isAlive)
                deletedResources.add(resource)

            for ((t, r) in result.produced)
                t.addDelayedResource(r)
        }

        if (deletedResources.isEmpty())
            return

        val oldKeysVersion = _resourcePack.keysVersion
        _resourcePack.removeAll(deletedResources)
        val keysVersion = _resourcePack.keysVersion

        // Try to remove the cached Resource instead of rebuilding DependencyMatches later
        for (matches in dependencyMatches.values)
            if (matches.keysVersion == oldKeysVersion) {
                matches.removeAll(deletedResources)
                matches.keysVersion = keysVersion
            }
    }

    fun levelUpdate() { //TODO works bad on Ice; wind should affect mountains mb they will stop growing
        for (i in 0 until if (type == Type.Water) 4 else if (level in 106..119) 5 else 1)
            erode()
    }

    fun <E> findUpdaterOfType(type: Class<E>): E? =
        updaters.filterIsInstance(type)
            .firstOrNull()

    private fun erode() {
        val tiles = neighbours.toMutableList()
        tiles.sortBy { it.secondLevel }
        val lowest = tiles[0]
        if (lowest.secondLevel + 1 < secondLevel) {
            setLevel(level - 1)
            lowest.setLevel(lowest.level + 1)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val tile = other as Tile
        return id == tile.id
    }

    override fun hashCode(): Int = id

    override fun toString() = "Tile at $posStr, type=$type, temperature=$prettyTemperature, level=$level" +
            "\nTags: " + tagPool.all.joinToString("; ") +
            "\nWind: ${wind.affectedTiles.joinToString { "${it.first.x} ${it.first.y}: ${it.second}" }}" +
            "\nFlow: ${flow.x} ${flow.y}" +
            "\n\nResources:" + _resourcePack.resources.joinToString("\n", "\n", "\n") +
            "\n\nArea: ${area}km^2"

    val prettyTemperature: String
        get() = "%.2f".format(temperature)

    val posStr = "$x $y"

    enum class Type {
        Normal, Mountain, Water, Ice, Woods, Growth
    }
}

const val MOUNTAIN_LEVEL = 110
