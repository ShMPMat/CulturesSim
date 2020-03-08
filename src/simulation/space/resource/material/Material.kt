package simulation.space.resource.material

import simulation.culture.aspect.Aspect
import simulation.space.resource.ResourceTag
import java.util.*

class Material(val name: String, val density: Double, val tags: List<ResourceTag>) {
    private val aspectConversion: MutableMap<Aspect, Material> = HashMap()
    fun addAspectConversion(aspect: Aspect, material: Material) {
        aspectConversion[aspect] = material
    }

    fun applyAspect(aspect: Aspect): Material {
        return aspectConversion[aspect] ?: this
    }

    fun hasTagWithName(name: String): Boolean {
        return tags.stream().anyMatch { tag: ResourceTag -> tag.name == name }
    }

    fun hasApplicationForAspect(aspect: Aspect): Boolean {
        return aspectConversion.containsKey(aspect)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val material = other as Material
        return name == material.name
    }

    override fun hashCode(): Int {
        return Objects.hash(name)
    }
}