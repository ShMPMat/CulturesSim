package io.tashtabash.sim.space.resource

import io.tashtabash.random.singleton.*
import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.resource.Taker.*
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.action.ResourceProbabilityAction
import io.tashtabash.sim.space.resource.dependency.ConsumeDependency
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.tile.Tile
import java.util.*
import kotlin.math.min
import kotlin.math.pow


open class Resource private constructor(
    val core: ResourceCore,
    amount: Int = core.genome.defaultAmount,
    hash: Int?,
    protected var deathTurn: Int = 0
) : Comparable<Resource> {
    constructor(core: ResourceCore, amount: Int = core.genome.defaultAmount) : this(core, amount, null)

    open var amount = amount
        protected set(value) {
            // It is assumed that a negative value can only be assigned on overflow
            field = if (value < 0)
                Int.MAX_VALUE
            else value
        }

    // Precomputed hash for quick comparisons
    private var _hash = 0

    //How many additional years added to this Resource due to bad environment. Large numbers results in sooner death
    protected var deathOverhead = 0

    //What part of this Resource will be destroyed on the next death event
    protected var deathPart = 1.0

    // Food left over by ConsumeDependencies; create later to not stress short-lived Resource copies
    private var consumeBuffers: IdentityHashMap<ConsumeDependency, Int>? = null

    internal fun getConsumeBuffer(dependency: ConsumeDependency): Int =
        consumeBuffers?.get(dependency) ?: 0

    internal fun setConsumeBuffer(dependency: ConsumeDependency, amount: Int) {
        if (amount <= 0)
            consumeBuffers?.remove(dependency)
        else
            (consumeBuffers ?: IdentityHashMap<ConsumeDependency, Int>(2).also { consumeBuffers = it })[dependency] =
                amount
    }

    private fun moveConsumeBuffers(from: Resource) {
        val otherBuffers = from.consumeBuffers
            ?: return
        from.consumeBuffers = null

        for ((dependency, amount) in otherBuffers)
            setConsumeBuffer(dependency, getConsumeBuffer(dependency) + amount)
    }

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

    fun getTagPresence(tag: ResourceTag) = amount * getTagLevel(tag) * genome.volume.pow(data.resourceSizeEffect)

    fun getTagLevel(tag: ResourceTag) = genome.getTagLevel(tag)

    open fun getPartInt(part: Int, taker: Taker): Int {
        val accessiblePart = amount * calculateAccessiblePart(taker)
        val result = when {
            part <= accessiblePart -> part
            else -> min(amount, accessiblePart.toInt() + 1) // Round up (or do +1, it doesn't matter)
        }

        amount -= result
        takers += taker to result

        hurtTaker(result, taker)

        return result
    }

    fun getPartInt(part: Int, resource: Resource) = getPartInt(part, ResourceTaker(resource))

    /**
     * @return Copy of this Resource with amount equal or less than requested.
     * Subtracts returned amount from the resource amount;
     */
    open fun getPart(part: Int, taker: Taker): Resource =
        copy(getPartInt(part, taker), deathTurn)

    private fun calculateAccessiblePart(taker: Taker): Double {
        var prob = RandomSingleton.random.nextDouble().pow(2) * .9

        prob -= genome.behaviour.camouflage + genome.behaviour.resistance + genome.behaviour.danger
        if (taker is ResourceTaker) {
            prob += taker.resource.genome.behaviour
                .let { it.danger + it.camouflage }

            val speed = genome.behaviour.speedMs
            val takerSpeed = taker.resource.genome.behaviour.speedMs
            prob -= when {
                takerSpeed != .0 -> (speed / takerSpeed - 1).coerceAtMost(.9)
                speed != .0 -> .9
                else -> .0
            }
        }

        return prob.coerceIn(.0, .99)
    }

    private fun hurtTaker(amount: Int, taker: Taker) {
        if (taker !is ResourceTaker)
            return

        val strength = genome.behaviour.danger / taker.resource.genome.behaviour.resistance
        val hurtPart = amount * strength
        if (hurtPart == .0)
            return

        taker.resource.getCleanPartInt(hurtPart.toInt(), ResourceTaker(this))
    }


    fun getPart(part: Int, resource: Resource) = getPart(part, ResourceTaker(resource))

    open fun getCleanPartInt(part: Int, taker: Taker?): Int {
        val result = min(amount, part)
        amount -= result
        if (taker != null)
            takers += taker to result

        return result
    }

    open fun getCleanPart(part: Int, taker: Taker?): Resource =
        copy(getCleanPartInt(part, taker), deathTurn)

    open fun merge(resource: Resource): Resource {
        if (resource.baseName != baseName)
            throw RuntimeException("Different resource tried to merge - $fullName and ${resource.fullName}")

        if (this === resource)
            return this

        addAmount(resource.amount, resource.deathPart * (resource.deathOverhead + resource.deathTurn) / genome.lifespan)
        moveConsumeBuffers(resource)
        resource.destroy()
        return this
    }

    fun swapOwnership(ownershipMarker: OwnershipMarker): Resource {
        val core = core.copy(ownershipMarker = ownershipMarker)
        val currentAmount = amount

        destroy()

        // The same population under a new owner keeps its food
        return core.resourceBuilder(core, currentAmount)
            .also { it.moveConsumeBuffers(this) }
    }

    fun copyWithOwnership(ownershipMarker: OwnershipMarker): Resource {
        val core = core.copy(ownershipMarker = ownershipMarker)
        return core.resourceBuilder(core, amount)
    }

    fun exactCopy() = copy(amount)

    open fun copy(amount: Int = genome.defaultAmount, deathTurn: Int = 0) =
        Resource(core, amount, _hash, deathTurn)

    fun fullCopy() = core.fullCopy()

    fun copyWithExternalFeatures(features: List<ExternalResourceFeature>): Resource {
        val resource = core.resourceBuilder(core.copyWithNewExternalFeatures(features), amount)
        // The same population with new features keeps its food
        resource.moveConsumeBuffers(this)
        destroy()
        return resource
    }

    fun copyWithNewExternalFeatures(features: List<ExternalResourceFeature>) =
        copyWithExternalFeatures(externalFeatures + features)

    open fun update(tile: Tile): ResourceUpdateResult {
        if (amount <= 0)
            return ResourceUpdateResult(false, emptyList())
        val result = mutableListOf<TiledResource>()

        val resources = genome.conversionCore.probabilityActions.flatMap { applyProbabilityAction(it, tile) }
        if (resources.any { (t, r) -> r.isAcceptable(t) })
            result += resources

        for (dependency in genome.dependencies) {
            val satisfaction = dependency.satisfactionPercent(tile, this)
            deathOverhead += ((1 - satisfaction) * genome.lifespan).toInt()
        }

        result += naturalDeath().map { tile to it }

        if (amount <= 0)
            return ResourceUpdateResult(false, result)
        deathTurn++

        expand(tile)
        distribute(tile)

        return ResourceUpdateResult(true, result)
    }

    protected open fun naturalDeath(): List<Resource> {
        val shouldDie = deathTurn + deathOverhead >= genome.lifespan
        if (!shouldDie && (genome.lifespan != Double.POSITIVE_INFINITY || deathOverhead != Int.MAX_VALUE))
            return emptyList()

        val deadAmount = calculateDeadAmount()
        takers += DeathTaker to deadAmount
        amount -= deadAmount
        deathTurn = 0
        deathOverhead = 0
        deathPart = 1.0
        return applyActionOrEmpty(specialActions.getValue("_OnDeath_"), deadAmount)
    }

    protected open fun calculateDeadAmount() = (deathPart * amount).toInt()

    private fun applyProbabilityAction(action: ResourceProbabilityAction, tile: Tile): List<TiledResource> {
        val expectedValue = amount * action.probability
        val maxPart = if (expectedValue < 1.0)
            expectedValue.chanceOf<Double> { 1.0 }
                ?: return emptyList()
        else expectedValue
        val satisfactionCoefficient = action.dependencies
            .minOfOrNull { it.satisfactionPercent(tile, this) }
            ?: 1.0
        val part = (maxPart * satisfactionCoefficient).toInt()
        if (part == 0)
            return emptyList()

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

    fun areNecessaryDependenciesSatisfied(tile: Tile) = genome.necessaryDependencies.all {
        it.satisfactionPercent(tile, this, true) == 1.0
    }

    fun isAcceptable(tile: Tile, threshold: Double = 0.8) = genome.dependencies.all {
        it.satisfactionPercent(tile, this, true) >= threshold
    }

    private fun distribute(tile: Tile) {
        val naturalDensity = genome.naturalDensity(tile.area)
        if (amount <= naturalDensity)
            return

        when (genome.behaviour.overflowType) {
            OverflowType.Migrate -> {
                val tiles = tile.getNeighbours { areNecessaryDependenciesSatisfied(it) }
                    .map { it to it.resourcePack.getAmount(this) } // Cache amounts
                    .sortedBy { it.second }
                    .map { it.first }

                for (neighbour in tiles) {
                    if (amount <= naturalDensity / 2)
                        break

                    var part = min(
                        amount - naturalDensity / 2,
                        naturalDensity - neighbour.resourcePack.getAmount(this)
                    )
                    part = if (part <= 0)
                        (amount - naturalDensity / 2) / tiles.size
                    else part

                    neighbour.addDelayedResource(getCleanPart(part, SeparationTaker))
                }
            }

            OverflowType.Cut -> amount = naturalDensity
            OverflowType.Ignore -> {}
        }
    }

    open fun addAmount(otherAmount: Int, otherDeathPart: Double = 0.0) {
        if (otherAmount > 0)
            deathPart = (amount * deathPart + otherAmount * otherDeathPart) / (amount + otherAmount)
        this.amount += otherAmount
    }

    fun applyAction(action: ResourceAction, part: Int = 1): Resources {
        val result = genome.conversionCore.applyAction(action) ?: listOf(copy(part))
        result.forEach { it.amount *= part }
        return result
    }

    fun applyActionUnsafe(action: ResourceAction) = genome.conversionCore.actionConversions[action]
        ?: core.wrappedSample

    fun applyActionOrEmpty(action: ResourceAction, part: Int = 1): List<Resource> {
        val result = genome.conversionCore.applyAction(action)
            ?: listOf()
        result.forEach { it.amount *= part }
        return result
    }

    fun hasApplicationForAction(action: ResourceAction) = genome.conversionCore.hasApplication(action)

    fun die() {
        takers += DeathTaker to amount
        destroy()
    }

    fun destroy() {
        amount = 0
    }

    fun applyActionAndConsume(action: ResourceAction, part: Int, isClean: Boolean, taker: Taker): Resources {
        val resourcePart =
            if (isClean) getCleanPart(part, taker)
            else getPart(part, taker)

        return resourcePart.applyAction(action, resourcePart.amount)
    }

    private fun expand(tile: Tile): Boolean = (genome.spreadProbability * amount).chanceOf<Boolean> {
        val newTile = tile.neighbours.shuffled(RandomSingleton.random)
            .firstOrNull { t -> genome.dependencies.all { it.hasNeeded(t) } }
            ?: if (genome.dependencies.all { it.hasNeeded(tile) })
                tile
            else .2.chanceOf<Tile> {
                tile
            } ?: tile.neighbours.randomElement()

        val amount = min(genome.naturalDensity(newTile.area), (genome.spreadProbability * amount).toInt())
        // Ensure that at least one Resource is spawned for small amounts and spreadProbabilities
        val resource = copy(amount.coerceAtLeast(1))
        newTile.addDelayedResource(resource)

        return true
    } ?: false

    override fun equals(other: Any?) =
        fullEquals(other)

    fun fullEquals(o: Any?) =
        equalsWithoutOwnership(o) && core.ownershipMarker == (o as Resource).core.ownershipMarker

    fun equalsWithoutOwnership(o: Any?): Boolean {
        if (this === o) return true
        if (o == null) return false
        val resource = o as Resource

        if (_hash != resource._hash)
            return false

        // Check core ref first, which is cheaper
        return core === resource.core || fullName == resource.fullName
    }

    override fun hashCode() = _hash

    override fun toString() = "Resource $fullName," +
            " spread probability - ${genome.spreadProbability}, mass - ${genome.mass}," +
            " lifespan - ${genome.lifespan}, default amount - ${genome.defaultAmount}, amount - $amount," +
            " material - ${genome.primaryMaterial}, ${genome.appearance}, ownership - ${core.ownershipMarker}" +
            "\n${genome.behaviour}, tags: " +
            tags.joinToString(" ")

    override fun compareTo(other: Resource): Int {
        val nameCompare = fullName.compareTo(other.fullName)

        return if (nameCompare == 0)
            core.ownershipMarker.compareTo(other.core.ownershipMarker)
        else nameCompare
    }
}
