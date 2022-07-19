package shmp.simulation.space.resource

import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElementOrNull
import shmp.random.singleton.randomTileOnBrink
import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.resource.Taker.*
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.action.ResourceProbabilityAction
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.tile.Tile
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


var n = 0
var s = 0.0

open class Resource private constructor(
        val core: ResourceCore,
        amount: Int = core.genome.defaultAmount,
        hash: Int?,
        protected var deathTurn: Int = 0
) : Comparable<Resource> {
    constructor(core: ResourceCore, amount: Int = core.genome.defaultAmount) : this(core, amount, null)

    open var amount = amount
        protected set(value) {
            field = if (value < 0)
                Int.MAX_VALUE
            else value
        }

    // Precomputed hash.
    private var _hash = 0

    //How many additional years added to this Resource due to bad environment. Large numbers results in sooner death.
    protected var deathOverhead = 0

    //What part of this Resource will be destroyed on the next death.
    protected var deathPart = 1.0

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

    inline val externalFeatures: List<ExternalResourceFeature>
        get() = core.externalFeatures

    inline val fullName: String
        get() = core.fullName

    inline val ownershipMarker: OwnershipMarker
        get() = core.ownershipMarker

    val takers = mutableListOf<Pair<Taker, Int>>()

    val genome: Genome
        get() = core.genome

    init {
        _hash = hash ?: computeHash()
    }

    fun getTagPresence(tag: ResourceTag) = amount * getTagLevel(tag) * genome.size.pow(data.resourceSizeEffect)

    fun getTagLevel(tag: ResourceTag) = genome.getTagLevel(tag)

    /**
     * @return Copy of this Resource with amount equal or less than requested.
     * Subtracts returned amount from the resource amount;
     */
    open fun getPart(part: Int, taker: Taker): Resource {
        val accessiblePart = amount * calculateAccessiblePart(taker)
        val result = when {
            part <= accessiblePart -> min(amount, part)
            accessiblePart + 1 < amount -> accessiblePart.toInt() + 1
            else -> amount
        }

        amount -= result
        takers.add(taker to result)

        hurtTaker(result, taker)

        return copy(result, deathTurn)
    }

    private fun calculateAccessiblePart(taker: Taker): Double {
        var prob = RandomSingleton.random.nextDouble().pow(2) * 0.9

        prob -= genome.behaviour.camouflage + genome.behaviour.resistance + genome.behaviour.danger
        if (taker is ResourceTaker)
            prob += taker.resource.genome.behaviour
                    .let { it.danger + it.camouflage }

        return max(min(prob, 1.0), 0.0)
    }

    private fun hurtTaker(amount: Int, taker: Taker) {
        if (taker !is ResourceTaker)
            return

        val strength = genome.behaviour.danger / taker.resource.genome.behaviour.resistance
        val hurtPart = amount * strength

        taker.resource.getCleanPart(hurtPart.toInt(), ResourceTaker(this)).destroy()
    }


    fun getPart(part: Int, resource: Resource) = getPart(part, ResourceTaker(resource))

    fun getCleanPart(part: Int, taker: Taker): Resource {
        val result = min(amount, part)
        amount -= result

        takers.add(taker to result)

        return copy(result, deathTurn)
    }

    open fun merge(resource: Resource): Resource {
        if (resource.baseName != baseName)
            throw RuntimeException("Different resource tried to merge - $fullName and ${resource.fullName}")

        if (this === resource)
            return this

        smartAddAmount(resource.amount, resource.deathPart)
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

    fun copy(amount: Int = genome.defaultAmount, deathTurn: Int = 0) =
            Resource(core, amount, _hash, deathTurn)

    fun copyAndDestroy(amount: Int = genome.defaultAmount): Resource {
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
        val result = mutableListOf<TiledResource>()
        if (amount <= 0)
            return ResourceUpdateResult(false, result)

        val resources = genome.conversionCore.probabilityActions.flatMap { applyProbabilityAction(it, tile) }
        if (resources.any { (t, r) -> r.isAcceptable(t) })
            result.addAll(resources)

        for (dependency in genome.dependencies) {
            val satisfaction = dependency.satisfactionPercent(tile, this)
            deathOverhead += ((1 - satisfaction) * genome.lifespan).toInt()
        }

        result.addAll(naturalDeath().map { tile to it })

        if (amount <= 0)
            ResourceUpdateResult(false, result)
        deathTurn++
        genome.spreadProbability.chanceOf {
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

    protected open fun naturalDeath(): List<Resource> {
        if (deathTurn + deathOverhead < genome.lifespan)
            return emptyList()

        val deadAmount = (deathPart * amount).toInt()
        takers.add(DeathTaker to deadAmount)
        amount -= deadAmount
        deathTurn = 0
        deathOverhead = 0
        deathPart = 1.0
        return applyActionOrEmpty(specialActions.getValue("_OnDeath_"), deadAmount)
    }

    private fun applyProbabilityAction(action: ResourceProbabilityAction, tile: Tile): List<TiledResource> {
        val expectedValue = amount * action.probability
        val part = if (expectedValue < 1.0)
            expectedValue.chanceOf<Int> { 1 } ?: 0
        else expectedValue.toInt()

        val result = if (action.isWasting)
            applyActionAndConsume(action, part, true, SelfTaker)
        else
            applyAction(action, part)
        val targetTile = if (action.canChooseTile)
            (tile.neighbours + tile).filter { isAcceptable(it) }
                    .randomElementOrNull()
                    ?: tile
        else tile

        return result.map { targetTile to it }
    }

    fun isIdeal(tile: Tile) = genome.necessaryDependencies.all { it.satisfactionPercent(tile, this) == 1.0 }

    fun isAcceptable(tile: Tile) = genome.dependencies.all { it.satisfactionPercent(tile, this) >= 0.8 }

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

                    neighbour.addDelayedResource(getCleanPart(part, SeparationTaker))
                }
            }
            OverflowType.Cut -> amount = genome.naturalDensity
            OverflowType.Ignore -> {
            }
        }
    }

    open fun addAmount(amount: Int) = smartAddAmount(amount)

    private fun smartAddAmount(otherAmount: Int, otherDeathPart: Double = 1.0) {
        if (otherAmount > 0)
            deathPart = (amount * deathPart + otherAmount * otherDeathPart) / (amount + otherAmount)
        this.amount += otherAmount
    }

    fun applyAction(action: ResourceAction, part: Int = 1): Resources {
        val result = genome.conversionCore.applyAction(action) ?: listOf(copy(part))
        result.forEach { it.amount *= part }
        return result
    }

    fun applyActionUnsafe(action: ResourceAction) = genome.conversionCore.actionConversion[action]
            ?: core.wrappedSample

    fun applyActionOrEmpty(action: ResourceAction, part: Int = 1): List<Resource> {
        val result = genome.conversionCore.applyAction(action)
                ?: listOf()
        result.forEach { it.amount *= part }
        return result
    }

    fun hasApplicationForAction(action: ResourceAction) = genome.conversionCore.hasApplication(action)

    fun destroy() {
        takers.add(DeathTaker to amount)
        amount = 0
    }

    open fun applyActionAndConsume(action: ResourceAction, part: Int, isClean: Boolean, taker: Taker): Resources {
        val resourcePart =
                if (isClean) getCleanPart(part, taker)
                else getPart(part, taker)

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
        val resource = copy(amount = min(genome.defaultAmount, amount))
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
            "lifespan - ${genome.lifespan}, amount - $amount, material - ${genome.primaryMaterial}," +
            " ${genome.appearance}, ownership - ${core.ownershipMarker}, tags: " +
            tags.joinToString(" ")

    override fun compareTo(other: Resource): Int {
        val nameCompare = fullName.compareTo(other.fullName)

        return if (nameCompare == 0)
            core.ownershipMarker.compareTo(other.core.ownershipMarker)
        else nameCompare
    }
}
