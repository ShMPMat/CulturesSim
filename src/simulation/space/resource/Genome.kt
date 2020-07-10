package simulation.space.resource

import simulation.space.SpaceData.data
import simulation.space.resource.dependency.ResourceDependency
import simulation.space.resource.instantiation.GenomeTemplate
import simulation.space.resource.material.Material
import simulation.space.resource.tag.ResourceTag
import java.util.*
import kotlin.math.ceil

open class Genome constructor(
        var name: String,
        val type: ResourceType,
        val size: Double,
        var spreadProbability: Double,
        val baseDesirability: Int,
        val overflowType: OverflowType,
        val isMutable: Boolean,
        val isMovable: Boolean,
        val isResisting: Boolean,
        val isDesirable: Boolean,
        val hasLegacy: Boolean,
        val lifespan: Double,
        val defaultAmount: Int,
        var legacy: ResourceCore?,
        val templateLegacy: ResourceCore?,
        dependencies: List<ResourceDependency>,
        tags: List<ResourceTag>,
        var primaryMaterial: Material?,
        secondaryMaterials: List<Material>
) {
    val naturalDensity: Int
    val parts: MutableList<Resource> = ArrayList()
    val dependencies = dependencies.toMutableList()
    val tags: MutableList<ResourceTag>
    val secondaryMaterials: List<Material>

    init {
        this.tags = ArrayList(tags)
        naturalDensity = ceil(data.resourceDenseCoefficient * defaultAmount).toInt()
        if (naturalDensity > 1000000000)
            System.err.println("Very high density in Genome $name - $naturalDensity")
        computePrimaryMaterial()
        computeTagsFromMaterials()
        this.secondaryMaterials = ArrayList(secondaryMaterials)
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
            legacy: ResourceCore? = this.legacy,
            templateLegacy: ResourceCore? = this.templateLegacy,
            dependencies: List<ResourceDependency> = this.dependencies,
            tags: List<ResourceTag> = this.tags,
            primaryMaterial: Material? = this.primaryMaterial,
            secondaryMaterials: List<Material> = this.secondaryMaterials,
            parts: MutableList<Resource> = this.parts
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
                templateLegacy,
                dependencies,
                tags,
                primaryMaterial,
                secondaryMaterials
        )
        parts.forEach { genome.addPart(it) }
        return genome
    }

    private fun computePrimaryMaterial() {
        if (parts.size == 1)
            primaryMaterial = parts[0].core.materials[0]
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
            //TODO hell
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

    val baseName: String
        get() = name + if (hasLegacy) legacyPostfix else ""

    private val legacyPostfix: String
        get() = (if (templateLegacy == null) "" else "_of_" + templateLegacy.genome.name + templateLegacy.genome.legacyPostfix) +
                if (legacy == null) "" else "_of_" + legacy!!.genome.name + legacy!!.genome.legacyPostfix

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
