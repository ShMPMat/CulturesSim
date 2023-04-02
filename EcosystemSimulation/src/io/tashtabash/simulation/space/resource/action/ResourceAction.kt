package io.tashtabash.simulation.space.resource.action

import io.tashtabash.simulation.space.resource.dependency.ResourceDependency


open class ResourceAction(
        val name: String,
        val tags: List<ActionTag>,
        val dependencies: List<ResourceDependency>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResourceAction) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode() = name.hashCode()

    open fun copy(
            name: String = this.name,
            tags: List<ActionTag> = this.tags,
            dependencies: List<ResourceDependency> = this.dependencies
    ) = ResourceAction(name, tags, dependencies)
}


class ResourceProbabilityAction(
        val baseName: String,
        val probability: Double,
        val isWasting: Boolean,
        val canChooseTile: Boolean,
        dependencies: List<ResourceDependency>
) : ResourceAction("_${baseName}_prob_${probability}", listOf(), dependencies) {
    fun copy(
            baseName: String = this.baseName,
            probability: Double = this.probability,
            isWasting: Boolean = this.isWasting,
            canChooseTile: Boolean = this.canChooseTile,
            dependencies: List<ResourceDependency> = this.dependencies
    ) = ResourceProbabilityAction(baseName, probability, isWasting, canChooseTile, dependencies)

    override fun copy(
            name: String,
            tags: List<ActionTag>,
            dependencies: List<ResourceDependency>,
    ) =  ResourceProbabilityAction(baseName, probability, isWasting, canChooseTile, dependencies)
}
