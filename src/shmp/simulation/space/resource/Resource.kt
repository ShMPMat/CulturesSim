package shmp.simulation.space.resource

import shmp.random.randomTileOnBrink
import shmp.random.singleton.chanceOf
import shmp.random.singleton.otherwise
import shmp.random.testProbability
import shmp.simulation.Controller
import shmp.simulation.event.Event
import shmp.simulation.space.SpaceData
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.action.ResourceProbabilityAction
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.tile.Tile
import java.util.*
import kotlin.math.min
import kotlin.math.pow


open class Resource(
        internal val core: ResourceCore,
        amount: Int = core.genome.defaultAmount,
        val ownershipMarker: OwnershipMarker = freeMarker
): Comparable<Resource> {
    open var amount = amount
        set(value) {
            if (value < 0)
                field = Int.MAX_VALUE
            field = value
        }

    // Precomputed hash.
    private var _hash = 0

    //How many turns has this Resource been existing.
    private var deathTurn = 0

    //How many additional years added to this Resource due to bad environment. Large numbers results in sooner death.
    private var deathOverhead = 0

    //What part of this Resource will be destroyed on the next death.
    private var deathPart = 1.0
    private val events: MutableList<Event> = ArrayList()

    val isEmpty: Boolean
        get() = amount == 0

    val isNotEmpty: Boolean
        get() = !isEmpty

    fun computeHash() {
        _hash = Objects.hash(fullName, core.hashCode(), ownershipMarker)
    }

    val simpleName: String
        get() = genome.name

    val baseName: BaseName
        get() = genome.baseName

    val tags: List<ResourceTag>
        get() = genome.tags

    val externalFeatures = core.externalFeatures

    val fullName = genome.baseName +
                if (externalFeatures.isNotEmpty())
                    externalFeatures.joinToString("_", "_") { it.name }
                else ""

    init {
        computeHash()
//        events.add(Event(Event.Type.Creation, "Resource was created", "name", fullName))
    }

    fun getTagPresence(tag: ResourceTag) =
            (amount * getTagLevel(tag)).toDouble() * genome.size.pow(SpaceData.data.resourceSizeEffect)

    fun getTagLevel(tag: ResourceTag) = tags.firstOrNull { it == tag }?.level ?: 0

    /**
     * Returns part of this resource and subtracts its amount from this resource amount;
     * @return Copy of this Resource with amount equal or less than requested.
     * Exact amount depends on current amount of this Resource.
     */
    open fun getPart(part: Int): Resource {
        val prob = SpaceData.data.random.nextDouble() * 0.5
        val result = when {
            part <= amount * prob -> min(amount, part)
            amount * prob + 1 < amount -> (amount * prob).toInt() + 1
            else -> amount
        }
        amount -= result

        return copy(result)
    }

    fun getCleanPart(part: Int): Resource {
        val result = min(amount, part)
        amount -= result
        return copy(result)
    }

    val genome: Genome
        get() = core.genome

    open fun merge(resource: Resource): Resource {
        if (resource.baseName != baseName) throw RuntimeException(String.format(
                "Different resource tried to merge - %s and %s",
                fullName,
                resource.fullName
        ))
        if (this === resource)
            return this

        addAmount(resource.amount)
        resource.destroy()
        return this
    }

    fun exactCopy(ownershipMarker: OwnershipMarker = this.ownershipMarker) = copy(amount, ownershipMarker)

    fun exactCopyAndDestroy(ownershipMarker: OwnershipMarker = this.ownershipMarker) =
            copyAndDestroy(amount, ownershipMarker)

    fun copy(amount: Int = genome.defaultAmount, ownershipMarker: OwnershipMarker = this.ownershipMarker) =
            Resource(core, amount, ownershipMarker)

    fun copyAndDestroy(
            amount: Int = genome.defaultAmount,
            ownershipMarker: OwnershipMarker = this.ownershipMarker
    ): Resource {
        val result = Resource(core, amount, ownershipMarker)
        destroy()
        return result
    }

    fun fullCopy() = core.fullCopy(ownershipMarker)

    fun copyWithExternalFeatures(features: List<ExternalResourceFeature>): Resource {
        val resource = Resource(core.copyWithNewExternalFeatures(features), amount)
        destroy()
        return resource
    }

    fun copyWithNewExternalFeatures(features: List<ExternalResourceFeature>) = copyWithExternalFeatures(externalFeatures + features)

    open fun update(tile: Tile): ResourceUpdateResult {
        val result = mutableListOf<Resource>()
        if (amount <= 0)
            return ResourceUpdateResult(false, result)

        result.addAll(genome.conversionCore.probabilityActions.flatMap { applyProbabilityAction(it) })

        for (dependency in core.genome.dependencies) {
            val part = dependency.satisfactionPercent(tile, this)
            deathOverhead += ((1 - part) * core.genome.lifespan).toInt()
        }

        if (deathTurn + deathOverhead >= core.genome.lifespan) {
            val deadAmount = (deathPart * amount).toInt()
            amount -= deadAmount
            deathTurn = 0
            deathOverhead = 0
            deathPart = 1.0
            result.addAll(applyAction(specialActions.getValue("_OnDeath_"), deadAmount))
        }

        if (amount <= 0)
            ResourceUpdateResult(false, result)
        deathTurn++
        core.genome.spreadProbability.chanceOf {
            expand(tile)
        }

        if (simpleName == "Vapour") {
            if (tile.temperature < 0) {
                tile.addDelayedResource(SpaceData.data.resourcePool.getBaseName("Snow").copy(amount / 2))
                amount -= amount / 2
            }
        }

        distribute(tile)

        return ResourceUpdateResult(true, result)
    }

    private fun applyProbabilityAction(action: ResourceProbabilityAction): List<Resource> {
        val expectedValue = amount * action.probability
        val part =
                if (expectedValue < 1.0)
                    expectedValue.chanceOf<Int> {
                        1
                    } ?: 0
                else expectedValue.toInt()

        return if (action.isWasting)
            applyActionAndConsume(action, part, true)
        else
            applyAction(action, part)
    }

    fun isAcceptable(tile: Tile) =
            genome.necessaryDependencies.all { it.satisfactionPercent(tile, this) == 1.0 }

    fun isOptimal(tile: Tile) = isAcceptable(tile)
            && genome.negativeDependencies.all { it.satisfactionPercent(tile, this) >= 0.9 }

    private fun distribute(tile: Tile) {
        if (amount <= genome.naturalDensity)
            return

        when (genome.overflowType) {
            OverflowType.Migrate -> {
                val tiles = tile.getNeighbours { isAcceptable(it) }
                        .sortedBy { it.resourcePack.getAmount(this) }

                for (neighbour in tiles) {
                    if (amount <= genome.naturalDensity / 2)
                        break

                    var part = min(
                            amount - genome.naturalDensity / 2,
                            genome.naturalDensity - neighbour.resourcePack.getAmount(this)
                    )
                    part = if (part <= 0)
                        (amount - genome.naturalDensity / 2) / tiles.size
                    else part

                    neighbour.addDelayedResource(getCleanPart(part))
                }
            }
            OverflowType.Cut -> amount = genome.naturalDensity
            OverflowType.Ignore -> {
            }
        }
    }

    open fun addAmount(amount: Int) {
        if (amount > 0)
            deathPart = this.amount * deathPart / (this.amount + amount)
        this.amount += amount
    }

    fun applyAction(action: ResourceAction, part: Int = 1): List<Resource> {
        val result = genome.conversionCore.applyAction(action) ?: listOf(copy(1))
        result.forEach { it.amount *= part }
        return result
    }

    fun hasApplicationForAction(action: ResourceAction) = genome.conversionCore.hasApplication(action)

    fun destroy() {
        amount = 0
    }

    open fun applyActionAndConsume(action: ResourceAction, part: Int, isClean: Boolean): List<Resource> {
        val resourcePart =
                if (isClean)
                    getCleanPart(part)
                else
                    getPart(part)

        return resourcePart.applyAction(action, resourcePart.amount)
    }

    private fun expand(tile: Tile): Boolean {
        val tileList = mutableListOf(tile)

        var newTile = randomTileOnBrink(tileList, SpaceData.data.random) {
            isAcceptable(it) && genome.dependencies.all { d -> d.hasNeeded(it) }
        }
        if (newTile == null) {
            if (genome.dependencies.all { it.hasNeeded(tile) })
                newTile = tile
            else {
                newTile = randomTileOnBrink(tileList, SpaceData.data.random) { isAcceptable(it) }
                if (newTile == null)
                    newTile = tile
            }
        }
        val resource = copy()
        resource.amount = min(core.genome.defaultAmount, amount)
        newTile.addDelayedResource(resource)
        return true
    }

    override fun equals(other: Any?) = fullEquals(other)

    fun ownershiplessEquals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null) return false
        val resource = o as Resource

        return if (_hash == resource._hash)
            fullName == resource.fullName
        else false
    }

    fun fullEquals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null) return false
        val resource = o as Resource
        return if (_hash != resource._hash)
            false
        else
            fullName == resource.fullName && ownershipMarker == resource.ownershipMarker
    }

    override fun hashCode() = _hash

    override fun toString() = "Resource $fullName, natural density - ${genome.naturalDensity}" +
            ", spread probability - ${genome.spreadProbability}, mass - ${genome.mass}, " +
            "lifespan - ${genome.lifespan}, amount - $amount, ownership - $ownershipMarker, tags: " +
            tags.joinToString(" ") { it.name }

    override fun compareTo(other: Resource): Int {
        val nameCompare = fullName.compareTo(other.fullName)

        return if (nameCompare == 0)
            ownershipMarker.compareTo(other.ownershipMarker)
        else nameCompare
    }
}
