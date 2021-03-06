package shmp.simulation.space.tile

import shmp.simulation.DataInitializationError
import shmp.simulation.space.SpaceData
import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.TectonicPlate
import shmp.simulation.space.WorldMap
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.container.ResourcePack
import java.util.*
import kotlin.math.max
import kotlin.math.pow


class Tile(val x: Int, val y: Int, private val typeUpdater: TypeUpdater) {
    val tagPool = MutableTileTagPool()

    var type: Type? = null
        internal set

    var plate: TectonicPlate? = null

    private val _resourcePack = MutableResourcePack()
    val resourcePack: ResourcePack
        get() = _resourcePack

    //Resources added on this Tile on a last turn. They are
    //stored here before the end of the turn.
    private val _delayedResources: MutableList<Resource> = ArrayList()

    val resourceDensity
        get() = resourcePack.resources
                .map { it.amount * it.genome.size.pow(3) }
                .foldRight(0.0, Double::plus) / data.tileResourceCapacity

    var level = 0
        internal set

    //Lowest level of this Tile which corresponds to the ground level.
    var secondLevel = 0
        private set

    var temperature = 0
        private set

    var neighbours = listOf<Tile>()
        set(value) {
            if (field.isNotEmpty())
                throw DataInitializationError("Neighbours are already set")
            field = value
        }

    init {
        updateTemperature()
        setType(Type.Normal, true)
    }

    private val windCenter = WindCenter()

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

    fun getNeighbours(predicate: (Tile) -> Boolean) = neighbours.filter(predicate)

    fun getTilesInRadius(radius: Int): Set<Tile> {
        val tiles = mutableSetOf<Tile>()
        var outer = setOf<Tile>()

        if (radius > 0)
            tiles.addAll(neighbours)

        for (i in 0 until radius - 1) {
            outer = outer
                    .flatMap { it.neighbours }
                    .filter { !tiles.contains(it) }
                    .toSet()
            tiles.addAll(outer)
        }

        return tiles
    }

    fun getTilesInRadius(radius: Int, predicate: (Tile) -> Boolean) = getTilesInRadius(radius)
            .filter(predicate)
            .toSet()

    val wind: Wind
        get() = windCenter.wind

    fun setType(type: Type, updateLevel: Boolean) {
        if (type == this.type)
            return

        this.type = type

        if (!updateLevel)
            return

        when (type) {
            Type.Mountain -> {
                level = 110
                secondLevel = 110
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

    fun setLevel(level: Int) {
        this.level = level
        secondLevel = level
        type = Type.Normal

        if (level >= 110)
            type = Type.Mountain
        if (level < data.defaultWaterLevel)
            type = Type.Water
    }

    private fun addResource(resource: Resource) {
        if (resource.isEmpty)
            return
        _resourcePack.add(resource)
    }

    /**
     * Adds resources which will be available on this Tile on the next turn.
     *
     * @param resource resource which will be added.
     */
    fun addDelayedResource(resource: Resource) {
        if (resource.isEmpty)
            return

        if (resource.simpleName == "Person") {
            val a = 0
        }

        _delayedResources.add(resource)
    }

    fun addDelayedResources(resources: Collection<Resource>) = resources.forEach { addDelayedResource(it) }

    fun addDelayedResources(pack: ResourcePack) = addDelayedResources(pack.resources)

    fun removeResource(resource: Resource) {
        _resourcePack.remove(resource)
        _delayedResources.remove(resource)
    }

    fun startUpdate() { //TODO wind blows on 2 neighbour tiles
        resourcePack.resources.forEach { it.takers.clear() }

        updateResources()
        windCenter.startUpdate()
        windCenter.useWind(_resourcePack.resources)
        updateTemperature()
        updateType()
    }

    fun middleUpdate(map: WorldMap) {
        _delayedResources.forEach { addResource(it) }
        _delayedResources.clear()
        windCenter.middleUpdate(x, y, map)
    }

    fun finishUpdate() {
        windCenter.finishUpdate()
//        if (testProbability(data.clearSpan, data.random))
//            resourcePack.clearEmpty()
    }

    private fun updateTemperature() {
        val start = data.temperatureBaseStart
        val finish = data.temperatureBaseFinish
        val size = data.mapSizeX
        var levelShift = 0
        if (type == Type.Water) {
            levelShift -= 2
            val deepness = max(data.defaultWaterLevel - level, 0)
            levelShift -= deepness
        } else {
            val elevation = max(level - data.defaultWaterLevel, 0)
            levelShift -= elevation / 3
        }
        temperature = start + x * (finish - start) / size + levelShift
    }

    private fun updateResources() {
        val deletedResources: MutableList<Resource> = ArrayList()

        for (resource in _resourcePack.resources) {
            val result = resource.update(this)
            if (!result.isAlive)
                deletedResources.add(resource)
            addDelayedResources(result.produced)
        }

        _resourcePack.removeAll(deletedResources)
    }

    private fun updateType() {
        typeUpdater.updateType(this)

        if (type == Type.Water)
            addDelayedResource(SpaceData.data.resourcePool.getBaseName("Vapour"))
    }

    fun levelUpdate() { //TODO works bad on Ice; wind should affect mountains mb they will stop growing
        for (i in 0 until if (type == Type.Water) 4 else if (level in 106..119) 5 else 1)
            distributeLevel()
    }

    private fun distributeLevel() {
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
        return x == tile.x && y == tile.y
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun toString() = "Tile $posStr, type=$type, temperature=$temperature, level=$level\n" +
            "Tags: " + tagPool.all.joinToString("; ") + "\n\nResources:" +
            _resourcePack.resources.joinToString("\n", "\n", "\n")

    val posStr = "$x $y"

    enum class Type {
        Normal, Mountain, Water, Ice, Woods, Growth
    }
}