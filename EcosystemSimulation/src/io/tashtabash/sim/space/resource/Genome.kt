package io.tashtabash.sim.space.resource

import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.resource.action.ConversionCore
import io.tashtabash.sim.space.resource.dependency.ResourceDependency
import io.tashtabash.sim.space.resource.instantiation.GenomeTemplate
import io.tashtabash.sim.space.resource.material.Material
import io.tashtabash.sim.space.resource.tag.ResourceTag
import java.util.*
import kotlin.math.ceil
import kotlin.math.pow


open class Genome(
    val name: String,
    val type: ResourceType,
    val sizeRange: Pair<Double, Double>,
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
    dependencies: List<ResourceDependency>,
    tags: Set<ResourceTag>,
    val primaryMaterial: Material?,
    val secondaryMaterials: List<Material>,
    var conversionCore: ConversionCore
) {
    val size = (sizeRange.first + sizeRange.second) / 2
    val naturalDensity = ceil(data.resourceDenseCoefficient * defaultAmount).toInt()
    val parts: MutableList<Resource> = mutableListOf()

    val dependencies = dependencies.toList()
    val necessaryDependencies = dependencies.filter { it.isNecessary }

    var tagsMap = tags.associateWith { it }
        private set(value) {
            field = value
            tags = value.keys
        }
    var tags: Set<ResourceTag> = tagsMap.keys // Caching tags makes a turn ~3 times faster
        private set

    init {
        if (naturalDensity > 1000000000)
            System.err.println("Very high density in Genome $name - $naturalDensity")
        computeTagsFromMaterials()
    }

    open fun copy(
        name: String = this.name,
        type: ResourceType = this.type,
        sizeRange: Pair<Double, Double> = this.sizeRange,
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
        primaryMaterial: Material? = this.primaryMaterial,
        secondaryMaterials: List<Material> = this.secondaryMaterials,
        conversionCore: ConversionCore = this.conversionCore.copy(),
        parts: List<Resource> = this.parts
    ): Genome {
        val genome = Genome(
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
            conversionCore
        )
        parts.forEach { genome.addPart(it) }
        return genome
    }

    val materials: List<Material>
        get() {
            return if (primaryMaterial != null)
                secondaryMaterials + primaryMaterial
            else listOf()
        }

    fun computeTagsFromMaterials() {
        if (primaryMaterial == null && this !is GenomeTemplate)
            throw ExceptionInInitializerError("Resource $name has no materials")
        else if (primaryMaterial != null)
            computeTags()
    }

    private fun computeTags() {
        val newTags = tagsMap.toMutableMap()
        newTags += primaryMaterial!!.tags
            .filter { !newTags.containsKey(it) }
            .associateWith { it }
        for (matcher in data.additionalTags)
            matcher.updateGenome(this, newTags)
        tagsMap = newTags
    }

    fun getTagLevel(tag: ResourceTag) =
        tagsMap[tag]?.level
            ?: 0.0

    val baseName: BaseName = name + legacyPostfix

    private val legacyPostfix: String
        get() = legacy?.let { "_of_$it" }
            ?: ""

    val mass: Double
        get() {
            primaryMaterial ?: throw ExceptionInInitializerError("Resource $name has no material")
            return primaryMaterial.density * size.pow(3)
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
