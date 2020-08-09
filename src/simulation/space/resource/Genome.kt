package simulation.space.resource

import simulation.space.SpaceData.data
import simulation.space.resource.action.ConversionCore
import simulation.space.resource.dependency.ResourceDependency
import simulation.space.resource.instantiation.GenomeTemplate
import simulation.space.resource.material.Material
import simulation.space.resource.tag.ResourceTag
import java.util.*
import kotlin.math.ceil

open class Genome(
        val name: String,
        val type: ResourceType,
        val size: Double,
        val spreadProbability: Double,
        val baseDesirability: Int,
        val overflowType: OverflowType,
        val isMutable: Boolean,
        val isMovable: Boolean,
        val isResisting: Boolean,
        val isDesirable: Boolean,
        val hasLegacy: Boolean,
        val lifespan: Double,
        val defaultAmount: Int,
        val legacy: BaseName?,
        dependencies: List<ResourceDependency>,
        tags: List<ResourceTag>,
        var primaryMaterial: Material?,
        secondaryMaterials: List<Material>,
        var conversionCore: ConversionCore
) {
    val naturalDensity = ceil(data.resourceDenseCoefficient * defaultAmount).toInt()
    val parts: MutableList<Resource> = ArrayList()
    val dependencies = dependencies.toMutableList()
    val tags = tags.toMutableList()
    val secondaryMaterials: List<Material>

    init {
        if (naturalDensity > 1000000000)
            System.err.println("Very high density in Genome $name - $naturalDensity")
        computePrimaryMaterial()
        computeTagsFromMaterials()
        this.secondaryMaterials = secondaryMaterials.toMutableList()
    }

    fun copy(
            name: String = this.name,
            type: ResourceType = this.type,
            size: Double = this.size,
            spreadProbability: Double = this.spreadProbability,
            baseDesirability: Int = this.baseDesirability,
            overflowType: OverflowType = this.overflowType,
            isMutable: Boolean = this.isMutable,
            isMovable: Boolean = this.isMovable,
            isResisting: Boolean = this.isResisting,
            isDesirable: Boolean = this.isDesirable,
            hasLegacy: Boolean = this.hasLegacy,
            lifespan: Double = this.lifespan,
            defaultAmount: Int = this.defaultAmount,
            legacy: BaseName? = this.legacy,
            dependencies: List<ResourceDependency> = this.dependencies,
            tags: List<ResourceTag> = this.tags,
            primaryMaterial: Material? = this.primaryMaterial,
            secondaryMaterials: List<Material> = this.secondaryMaterials,
            conversionCore: ConversionCore = this.conversionCore.copy(),
            parts: List<Resource> = this.parts
    ): Genome {
        val genome = Genome(
                name,
                type,
                size,
                spreadProbability,
                baseDesirability,
                overflowType,
                isMutable,
                isMovable,
                isResisting,
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
        tags.addAll(primaryMaterial!!.tags.filter { it !in tags })
        for ((tag, labeler) in data.additionalTags)
            if (!tags.contains(tag) && labeler.isSuitable(this))
                tags.add(tag.copy())
    }

    val baseName: BaseName = name + legacyPostfix

    private val legacyPostfix: String
        get() = legacy?.let { "_of_$it" }
                ?: ""

    val mass: Double
        get() {
            val material = primaryMaterial
                    ?: return 0.0
            return material.density * size * size * size
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
