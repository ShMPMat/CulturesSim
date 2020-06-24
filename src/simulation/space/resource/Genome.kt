package simulation.space.resource

import simulation.space.SpaceData.data
import simulation.space.resource.dependency.ResourceDependency
import simulation.space.resource.dependency.TemperatureMax
import simulation.space.resource.dependency.TemperatureMin
import simulation.space.resource.instantiation.GenomeTemplate
import simulation.space.resource.material.Material
import simulation.space.resource.tag.ResourceTag
import simulation.space.tile.Tile
import java.util.*
import kotlin.math.ceil

open class Genome(
        var name: String,
        val type: Type,
        val size: Double,
        var spreadProbability: Double,
        val temperatureMin: Int,
        val temperatureMax: Int,
        val baseDesirability: Int,
        val canMove: Boolean,
        val isMutable: Boolean,
        val isMovable: Boolean,
        val isResisting: Boolean,
        isDesirable: Boolean,
        val hasLegacy: Boolean,
        val deathTime: Int,
        val defaultAmount: Int,
        var legacy: ResourceCore?,
        val templateLegacy: ResourceCore?,
        dependencies: List<ResourceDependency>,
        tags: List<ResourceTag>,
        var primaryMaterial: Material?,
        secondaryMaterials: List<Material>
) {
    val isDesirable = false
    val naturalDensity: Int
    val parts: MutableList<Resource> = ArrayList()
    val dependencies: MutableList<ResourceDependency>
    val tags: MutableList<ResourceTag>
    val secondaryMaterials: List<Material>

    init {
        this.tags = ArrayList(tags)
        this.dependencies = ArrayList(dependencies)
        this.dependencies.add(TemperatureMin(temperatureMin, 2.0))
        this.dependencies.add(TemperatureMax(temperatureMax, 2.0))
        naturalDensity = ceil(data.resourceDenseCoefficient * defaultAmount).toInt()
        if (naturalDensity > 1000000000)
            System.err.println("Very high density in Genome $name - $naturalDensity")
        computePrimaryMaterial()
        computeTagsFromMaterials()
        this.secondaryMaterials = ArrayList(secondaryMaterials)
    }

    fun copy(
            name: String = this.name,
            type: Type = this.type,
            size: Double = this.size,
            spreadProbability: Double = this.spreadProbability,
            temperatureMin: Int = this.temperatureMin,
            temperatureMax: Int = this.temperatureMax,
            baseDesirability: Int = this.baseDesirability,
            canMove: Boolean = this.canMove,
            isMutable: Boolean = this.isMutable,
            isMovable: Boolean = this.isMovable,
            isResisting: Boolean = this.isResisting,
            isDesirable: Boolean = this.isDesirable,
            hasLegacy: Boolean = this.hasLegacy,
            deathTime: Int = this.deathTime,
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
                temperatureMin,
                temperatureMax,
                baseDesirability,
                canMove,
                isMutable,
                isMovable,
                isResisting,
                isDesirable,
                hasLegacy,
                deathTime,
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
        if (parts.size == 1) {
            primaryMaterial = parts[0].core.materials[0]
        }
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
        tags.addAll(primaryMaterial!!.tags)
        for ((tag, labeler) in data.additionalTags) {
            if (!tags.contains(tag) && labeler.isSuitable(this)) {
                tags.add(tag.copy())
            }
        }
    }

    val baseName: String
        get() = name + if (hasLegacy) legacyPostfix else ""

    private val legacyPostfix: String
        get() = (if (templateLegacy == null) "" else "_of_" + templateLegacy!!.genome.name + templateLegacy!!.genome.legacyPostfix) +
                if (legacy == null) "" else "_of_" + legacy!!.genome.name + legacy!!.genome.legacyPostfix

    val mass: Double
        get() = if (primaryMaterial == null) 0.0 else primaryMaterial!!.density * size * size * size

    fun isAcceptable(tile: Tile?) =
            dependencies.filter { it.isNecessary }.all { it.satisfactionPercent(tile, null) == 1.0 }

    fun isOptimal(tile: Tile?) = isAcceptable(tile)
            && dependencies.filter { !it.isPositive }.all { it.satisfactionPercent(tile, null) >= 0.9 }

    fun addPart(part: Resource) {
        val i = parts.indexOf(part)
        if (i == -1) {
            parts.add(part)
            computePrimaryMaterial()
        } else
            parts[i].addAmount(part.amount)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val genome = o as Genome
        return baseName == genome.baseName
    }

    override fun hashCode(): Int {
        return Objects.hash(baseName)
    }

    enum class Type {
        Plant, Animal, Mineral, Building, Artifact, None
    }
}