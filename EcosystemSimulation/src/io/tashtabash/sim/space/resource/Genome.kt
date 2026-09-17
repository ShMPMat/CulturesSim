package io.tashtabash.sim.space.resource

import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.resource.action.ConversionCore
import io.tashtabash.sim.space.resource.dependency.ResourceDependency
import io.tashtabash.sim.space.resource.material.Material
import io.tashtabash.sim.space.resource.tag.ResourceTag
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


class Genome(
    val name: String,
    val type: ResourceType,
    val sizeRange: Pair<Size, Size>,
    val spreadProbability: Double,
    val baseDesirability: Int,
    val isMutable: Boolean,
    val isMovable: Boolean,
    val behaviour: Behaviour,
    val appearance: Appearance,
    val hasLegacy: Boolean,
    val lifespan: Double,
    val defaultAmount: Int,
    val legacy: BaseName?,
    val dependencies: List<ResourceDependency>,
    tags: Set<ResourceTag>,
    val primaryMaterial: Material,
    val secondaryMaterials: List<Material> = listOf(),
    var conversionCore: ConversionCore = ConversionCore(),
    val parts: MutableList<Resource> = mutableListOf()
) {
    val size = sizeRange.first.avg(sizeRange.second)
    val naturalDensity = ceil(data.resourceDenseCoefficient * defaultAmount).toInt()

    val necessaryDependencies = dependencies.filter { it.isNecessary }

    var tagsMap: Map<ResourceTag, ResourceTag> = tags.associateWith { it }
        private set(value) {
            field = value
            tags = value.keys
        }
    var tags: Set<ResourceTag> = tagsMap.keys // Caching tags makes a turn ~3 times faster
        private set

    init {
        computeTags()
    }

    fun copy(
        name: String = this.name,
        type: ResourceType = this.type,
        sizeRange: Pair<Size, Size> = this.sizeRange,
        spreadProbability: Double = this.spreadProbability,
        baseDesirability: Int = this.baseDesirability,
        isMutable: Boolean = this.isMutable,
        isMovable: Boolean = this.isMovable,
        behaviour: Behaviour = this.behaviour,
        appearance: Appearance = this.appearance,
        hasLegacy: Boolean = this.hasLegacy,
        lifespan: Double = this.lifespan,
        defaultAmount: Int = this.defaultAmount,
        legacy: BaseName? = this.legacy,
        dependencies: List<ResourceDependency> = this.dependencies,
        tags: Set<ResourceTag> = this.tags,
        primaryMaterial: Material = this.primaryMaterial,
        secondaryMaterials: List<Material> = this.secondaryMaterials,
        conversionCore: ConversionCore = this.conversionCore.copy(),
        parts: List<Resource> = this.parts
    ) = Genome(
        name,
        type,
        sizeRange,
        spreadProbability,
        baseDesirability,
        isMutable,
        isMovable,
        behaviour,
        appearance,
        hasLegacy,
        lifespan,
        defaultAmount,
        legacy,
        dependencies,
        tags,
        primaryMaterial,
        secondaryMaterials,
        conversionCore,
        parts.toMutableList()
    )

    val materials: List<Material>
        get() = secondaryMaterials + primaryMaterial

    private fun computeTags() {
        val newTags = tagsMap.toMutableMap()
        newTags += primaryMaterial.tags
            .filter { !newTags.containsKey(it) }
            .associateWith { it }
        for (matcher in data.additionalTags)
            matcher.updateGenome(this, newTags)
        tagsMap = newTags
    }

    fun getTagLevel(tag: ResourceTag) =
        tagsMap[tag]?.level
            ?: .0

    val baseName: BaseName = name + legacyPostfix

    private val legacyPostfix: String
        get() = legacy?.let { "_of_$it" }
            ?: ""

    val volume: Double = size.x * size.y * size.z

    val mass: Double by lazy { // Assuming that the first mass call will be made after all addPart(..)
        if (parts.isEmpty())
            primaryMaterial.overallDensity * volume
        else
            parts.sumOf { it.genome.mass * it.amount }
    }


    fun addPart(part: Resource) =
        if (!parts.contains(part))
            parts += part
        else throw ExceptionInInitializerError("Resource $name already has part ${part.baseName}")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val genome = other as Genome
        return baseName == genome.baseName
    }

    override fun hashCode(): Int = Objects.hash(baseName)
}

typealias BaseName = String

data class Size(val x: Double, val y: Double, val z: Double) {
    constructor(dimension: Double) : this(dimension, dimension, dimension)

    val max = max(x, max(y, z))
    val min = min(x, min(y, z))

    operator fun times(other: Double) = Size(x * other, y * other, z * other)

    fun avg(other: Size) = Size((x + other.x) / 2, (y + other.y) / 2, (z + other.z) / 2)
}
