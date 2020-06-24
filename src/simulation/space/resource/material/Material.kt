package simulation.space.resource.material

import simulation.space.resource.ResourceAction
import simulation.space.resource.tag.ResourceTag
import java.util.*

class Material(val name: String, val density: Double, val tags: List<ResourceTag>) {
    private val aspectConversion: MutableMap<ResourceAction, Material> = HashMap()

    fun addAspectConversion(action: ResourceAction, material: Material) {
        aspectConversion[action] = material
    }

    fun applyAction(action: ResourceAction): Material = aspectConversion[action] ?: this

    fun hasTagWithName(name: String) = tags.any { it.name == name }

    fun hasApplicationForAspect(action: ResourceAction) = aspectConversion.containsKey(action)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val material = other as Material
        return name == material.name
    }

    override fun hashCode() = Objects.hash(name)
}
