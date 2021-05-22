package shmp.simulation.space.resource

import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomTileOnBrink
import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.action.ResourceProbabilityAction
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.tile.Tile
import java.util.*
import kotlin.math.min
import kotlin.math.pow


open class Resource private constructor(
        internal val core: ResourceCore,
        amount: Int = core.genome.defaultAmount,
        hash: Int?
) : Comparable<Resource> {
    constructor(core: ResourceCore, amount: Int = core.genome.defaultAmount) : this(core, amount, null)

    open var amount = amount
        set(value) {
            field = if (value < 0)
                Int.MAX_VALUE
            else
                value
        }

    // Precomputed hash.
    private var _hash = 0

    //How many turns has this Resource been existing.
    private var deathTurn = 0

    //How many additional years added to this Resource due to bad environment. Large numbers results in sooner death.
    private var deathOverhead = 0

    //What part of this Resource will be destroyed on the next death.
    private var deathPart = 1.0

    inline val isEmpty: Boolean
        get() = amount == 0

    inline val isNotEmpty: Boolean
        get() = !isEmpty

    private fun computeHash() = Objects.hash(fullName, core.hashCode())

    inline val simpleName: String
        get() = genome.name

    inline val baseName: BaseName
        get() = genome.baseName

    inline val tags: Set<ResourceTag>
        get() = genome.tags

    val externalFeatures: List<ExternalResourceFeature>
        get() = core.externalFeatures

    val fullName = genome.baseName +
            if (externalFeatures.isNotEmpty())
                externalFeatures.joinToString("_", "_") { it.name }
            else ""

    val ownershipMarker: OwnershipMarker
        get() = core.ownershipMarker

    init {
        _hash = hash ?: computeHash()
    }

    fun getTagPresence(tag: ResourceTag) =
            (amount * getTagLevel(tag)).toDouble() * genome.size.pow(data.resourceSizeEffect)

    fun getTagLevel(tag: ResourceTag) = tags.firstOrNull { it == tag }?.level ?: 0

    /**
     * Returns part of this resource and subtracts its amount from this resource amount;
     * @return Copy of this Resource with amount equal or less than requested.
     * Exact amount depends on current amount of this Resource.
     */
    open fun getPart(part: Int): Resource {
        val prob = RandomSingleton.random.nextDouble() * 0.5
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

    fun exactCopy() = copy(amount)

    fun swapOwnership(ownershipMarker: OwnershipMarker): Resource {
        val core = core.copy(ownershipMarker = ownershipMarker)
        val currentAmount = amount

        destroy()

        return Resource(core, currentAmount)
    }

    fun copyWithOwnership(ownershipMarker: OwnershipMarker): Resource {
        val core = core.copy(ownershipMarker = ownershipMarker)
        return Resource(core, amount)
    }

    fun copy(amount: Int = genome.defaultAmount) =
            Resource(core, amount, _hash)

    fun copyAndDestroy(
            amount: Int = genome.defaultAmount
    ): Resource {
        val result = Resource(core, amount, _hash)
        destroy()
        return result
    }

    fun fullCopy() = core.fullCopy()

    fun copyWithExternalFeatures(features: List<ExternalResourceFeature>): Resource {
        val resource = Resource(core.copyWithNewExternalFeatures(features), amount)
        destroy()
        return resource
    }

    fun copyWithNewExternalFeatures(features: List<ExternalResourceFeature>) =
            copyWithExternalFeatures(externalFeatures + features)

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
                tile.addDelayedResource(data.resourcePool.getBaseName("Snow").copy(amount / 2))
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

    fun isIdeal(tile: Tile) =
            genome.necessaryDependencies.all { it.satisfactionPercent(tile, this) == 1.0 }

    fun isAcceptable(tile: Tile) =
            genome.negativeDependencies.all { it.satisfactionPercent(tile, this) >= 0.8 }
                    && genome.positiveDependencies.all { it.satisfactionPercent(tile, this) >= 0.8 }

    private fun distribute(tile: Tile) {
        if (amount <= genome.naturalDensity)
            return

        when (genome.behaviour.overflowType) {
            OverflowType.Migrate -> {
                val tiles = tile.getNeighbours { isIdeal(it) }
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
                if (isClean) getCleanPart(part)
                else getPart(part)

        return resourcePart.applyAction(action, resourcePart.amount)
    }

    private fun expand(tile: Tile): Boolean {
        val tileList = mutableListOf(tile)

        var newTile = tileList.randomTileOnBrink {
            isIdeal(it) && genome.dependencies.all { d -> d.hasNeeded(it) }
        }
        if (newTile == null) {
            if (genome.dependencies.all { it.hasNeeded(tile) })
                newTile = tile
            else {
                newTile = tileList.randomTileOnBrink { isIdeal(it) }
                if (newTile == null)
                    newTile = tile
            }
        }
        val resource = copy(amount = min(core.genome.defaultAmount, amount))
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
            fullName == resource.fullName && core.ownershipMarker == resource.core.ownershipMarker
    }

    override fun hashCode() = _hash

    override fun toString() = "Resource $fullName, natural density - ${genome.naturalDensity}" +
            ", spread probability - ${genome.spreadProbability}, mass - ${genome.mass}, " +
            "lifespan - ${genome.lifespan}, amount - $amount, colour - ${genome.appearance.colour}, " +
            "ownership - $core.ownershipMarker, tags: " +
            tags.joinToString(" ") { it.name }

    override fun compareTo(other: Resource): Int {
        val nameCompare = fullName.compareTo(other.fullName)

        return if (nameCompare == 0)
            core.ownershipMarker.compareTo(other.core.ownershipMarker)
        else nameCompare
    }
}
