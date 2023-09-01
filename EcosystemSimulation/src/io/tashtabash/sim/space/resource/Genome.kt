package io.tashtabash.sim.space.resource

import io.tashtabash.sim.space.SpaceData.data
import io.tashtabash.sim.space.resource.action.ConversionCore
import io.tashtabash.sim.space.resource.dependency.ResourceDependency
import io.tashtabash.sim.space.resource.instantiation.GenomeTemplate
import io.tashtabash.sim.space.resource.material.Material
import io.tashtabash.sim.space.resource.tag.ResourceTag
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
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
        val isDesirable: Boolean,
        val hasLegacy: Boolean,
        val lifespan: Double,
        val defaultAmount: Int,
        val legacy: BaseName?,
        dependencies: List<ResourceDependency>,
        tags: Set<ResourceTag>,
        var primaryMaterial: Material?,
        secondaryMaterials: List<Material>,
        var conversionCore: ConversionCore
) {
    val size = (sizeRange.first + sizeRange.second) / 2
    val naturalDensity = ceil(data.resourceDenseCoefficient * defaultAmount).toInt()
    val parts: MutableList<Resource> = ArrayList()

    val dependencies = dependencies.toList()
    val necessaryDependencies = dependencies.filter { it.isNecessary }
    val negativeDependencies = dependencies.filter { !it.isPositive }
    val positiveDependencies = dependencies.filter { it.isPositive }

    private val tagsMap = tags.map { it to it }.toMap().toMutableMap()
    var tags: Set<ResourceTag> = tagsMap.keys
        private set

    val secondaryMaterials: List<Material>

    init {
        if (naturalDensity > 1000000000)
            System.err.println("Very high density in Genome $name - $naturalDensity")
        computePrimaryMaterial()
        computeTagsFromMaterials()
        this.secondaryMaterials = secondaryMaterials.toMutableList()
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
            isDesirable: Boolean = this.isDesirable,
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
                isDesirable,
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

    private fun computePrimaryMaterial() {
        if (parts.size == 1)
            primaryMaterial = parts[0].genome.primaryMaterial
    }

    val materials: List<Material>
        get() {
            val materials = mutableListOf<Material>()
            val material = primaryMaterial//TODO remove when primaryMaterial is val
            if (material != null) {
                materials.add(material)
                materials.addAll(secondaryMaterials)
            }
            return materials
        }

    fun computeTagsFromMaterials() {
        if (primaryMaterial == null && this !is GenomeTemplate) {
            val k = 0//TODO hell
//            throw new ExceptionInInitializerError("Resource " + getName() + " has no materials.");
        } else if (primaryMaterial != null) {
            computeTags()
        }
    }

    private fun computeTags() {
        tagsMap.putAll(primaryMaterial!!.tags.filter { !tagsMap.containsKey(it) }.map { it to it }.toMap())
        for ((tag, labeler, leveler) in data.additionalTags) {
            val level = leveler.getLevel(this)
            if (tagsMap.containsKey(tag)) {
                val existingTag = tagsMap.getValue(tag)

                tagsMap[tag] = tag.copy(level = max(level, existingTag.level))
            } else if (labeler.isSuitable(this))
                tagsMap[tag] = tag.copy(level = level)
        }
        tags = tagsMap.keys
    }

    fun getTagLevel(tag: ResourceTag) = tagsMap[tag]?.level ?: 0.0

    val baseName: BaseName = name + legacyPostfix

    private val legacyPostfix: String
        get() = legacy?.let { "_of_$it" }
                ?: ""

    val mass: Double
        get() {
            val material = primaryMaterial
                    ?: return 0.0
            return material.density * size.pow(3)
        }

    fun addPart(part: Resource) {
        val i = parts.indexOf(part)
        if (i == -1) {
            parts.add(part)
            computePrimaryMaterial()
        } else
            parts[i].addAmount(part.amount)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val genome = other as Genome
        return baseName == genome.baseName
    }

    override fun hashCode(): Int = Objects.hash(baseName)
}

typealias BaseName = String
