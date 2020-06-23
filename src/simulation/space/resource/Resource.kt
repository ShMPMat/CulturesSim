package simulation.space.resource

import extra.SpaceProbabilityFuncs
import shmp.random.testProbability
import simulation.Controller
import simulation.Event
import simulation.SimulationException
import simulation.culture.aspect.Aspect
import simulation.culture.thinking.meaning.Meme
import simulation.space.resource.tag.AspectImprovementTag
import simulation.space.resource.tag.ResourceTag
import simulation.space.tile.Tile
import java.util.*
import kotlin.math.min

open class Resource(var resourceCore: ResourceCore, open var amount: Int) {
    // Precomputed hash.
    private var _hash = 0

    //How many turns has this Resource been existing.
    private var deathTurn = 0

    //How many additional years added to this Resource due to bad environment. Large numbers results in sooner death.
    private var deathOverhead = 0

    //What part of this Resource will be destroyed on the next death.
    private var deathPart = 1.0
    private val events: MutableList<Event> = ArrayList()
    var ownershipMarker = freeMarker

    val aspectConversion: Map<Aspect, List<Pair<Resource?, Int>>>
        get() = resourceCore.aspectConversion

    val isEmpty: Boolean
        get() {
            if (amount < 0)
                throw SimulationException("Resource amount is below zero: $this")
            return amount == 0
        }

    val isNotEmpty: Boolean
        get() = !isEmpty

    fun computeHash() {
        _hash = Objects.hash(fullName)
    }

    val simpleName: String
        get() = genome.name

    val baseName: String
        get() = genome.baseName

    val fullName: String
        get() = genome.baseName + resourceCore.meaningPostfix

    val tags: List<ResourceTag>
        get() = genome.tags

    init {
        computeHash()
        events.add(Event(Event.Type.Creation, "Resource was created", "name", fullName))
    }

    constructor(resourceCore: ResourceCore) : this(resourceCore, resourceCore.genome.defaultAmount)

    fun getAspectImprovement(aspect: Aspect): Double {
        return amount.toDouble() *
                tags.filterIsInstance<AspectImprovementTag>()
                        .filter { it.labeler.isSuitable(aspect) }
                        .map { it.improvement }
                        .foldRight(0.0, Double::plus)
    }

    fun getTagPresence(tag: ResourceTag) = (amount * getTagLevel(tag)).toDouble() /* * getGenome().getSize()*/

    fun getTagLevel(tag: ResourceTag) = tags.firstOrNull { it == tag }?.level ?: 0

    /**
     * Returns part of this resource and subtracts its amount from this resource amount;
     * @return Copy of this Resource with amount equal or less than requested.
     * Exact amount depends on current amount of this Resource.
     */
    open fun getPart(part: Int): Resource {
        val prob = Controller.session.random.nextDouble() * 0.5
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
        get() = resourceCore.genome

    fun hasMeaning() = resourceCore.hasMeaning

    fun setHasMeaning(b: Boolean) {
        resourceCore.hasMeaning = b
    }

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

    fun copy(): Resource {
        val resource = Resource(resourceCore)
        resource.ownershipMarker = ownershipMarker
        return resource
    }

    fun exactCopy() = copy(amount)

    fun copy(amount: Int): Resource {
        val resource = Resource(resourceCore, amount)
        resource.ownershipMarker = ownershipMarker
        return resource
    }

    fun fullCopy(): Resource {
        val resource = resourceCore.fullCopy()
        resource.ownershipMarker = ownershipMarker
        return resource
    }

    fun insertMeaning(meaning: Meme, postfix: String): Resource {
        val resource = Resource(resourceCore.insertMeaning(meaning, postfix), amount)
        destroy()
        return resource
    }

    open fun update(tile: Tile): ResourceUpdateResult? {
        var result: List<Resource> = ArrayList()
        if (amount <= 0)
            return ResourceUpdateResult(false, result)

        for (dependency in resourceCore.genome.dependencies) {
            val part = dependency.satisfactionPercent(tile, this)
            deathOverhead += ((1 - part) * resourceCore.genome.deathTime).toInt()
        }

        if (deathTurn + deathOverhead >= resourceCore.genome.deathTime) {
            val deadAmount = (deathPart * amount).toInt()
            amount -= deadAmount
            deathTurn = 0
            deathOverhead = 0
            deathPart = 1.0
            result = applyAspect(DEATH_ASPECT, deadAmount)
        }

        if (amount <= 0)
            ResourceUpdateResult(false, result)
        deathTurn++
        if (testProbability(resourceCore.genome.spreadProbability, Controller.session.random))
            expand(tile)

        if (simpleName == "Vapour") {
            if (tile.temperature < 0) {
                tile.addDelayedResource(Controller.session.world.resourcePool.get("Snow").copy(amount / 2))
                amount -= amount / 2
            }
        }

        distribute(tile)

        return ResourceUpdateResult(true, result)
    }

    private fun distribute(tile: Tile) {
        if (genome.canMove && amount > genome.naturalDensity) {
            val tiles = tile.getNeighbours { genome.isAcceptable(it) }
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
    }

    open fun addAmount(amount: Int) {
        if (amount > 0)
            deathPart = this.amount * deathPart / (this.amount + amount)
        this.amount += amount
    }

    @JvmOverloads
    fun applyAspect(aspect: Aspect, part: Int = 1): List<Resource> {
        val result = resourceCore.applyAspect(aspect)
        result.forEach { it.amount *= part }
        return result
    }

    fun hasApplicationForAspect(aspect: Aspect) = resourceCore.hasApplicationForAspect(aspect)

    fun destroy() {
        amount = 0
    }

    open fun applyAndConsumeAspect(aspect: Aspect, part: Int, isClean: Boolean): List<Resource> {
        val resourcePart = if (isClean)
            getCleanPart(part)
        else
            getPart(part)

        val p = min(part, resourcePart.amount)
        val result = resourcePart.resourceCore.applyAspect(aspect)
        result.forEach { it.amount *= p }
        resourcePart.amount -= p

        return result
    }

    private fun expand(tile: Tile): Boolean {
        val l = mutableListOf<Tile>()
        l.add(tile)
        var newTile = SpaceProbabilityFuncs.randomTileOnBrink(l) { t ->
            (genome.isAcceptable(t) && genome.dependencies.all { d -> d.hasNeeded(t) })
        }
        if (newTile == null) {
            if (genome.dependencies.all { d -> d.hasNeeded(tile) })
                newTile = tile
            else {
                newTile = SpaceProbabilityFuncs.randomTileOnBrink(l) { genome.isAcceptable(it) }
                if (newTile == null)
                    newTile = tile
            }
        }
        val resource = copy()
        resource.amount = min(resourceCore.genome.defaultAmount, amount)
        newTile.addDelayedResource(resource)
        return true
    }

    override fun equals(o: Any?) = fullEquals(o)

    fun ownershiplessEquals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null) return false
        val resource = o as Resource

        return if (_hash != resource._hash)
            false
        else
            fullName == resource.fullName
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

    override fun hashCode() = Objects.hash(fullName, resourceCore.hashCode(), ownershipMarker)

    override fun toString() = "Resource $fullName, natural density - ${genome.naturalDensity}" +
            ", spread probability - ${genome.spreadProbability}, mass - ${genome.mass}, amount - $amount, tags: " +
            tags.joinToString(" ") { it.name }
}