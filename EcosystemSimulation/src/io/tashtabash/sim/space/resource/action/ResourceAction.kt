package io.tashtabash.sim.space.resource.action

import io.tashtabash.sim.space.resource.dependency.ResourceDependency


open class ResourceAction(
        val technicalName: String,
        val tags: List<ActionTag>,
        val dependencies: List<ResourceDependency>,
        val name: String = technicalName
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResourceAction) return false

        if (technicalName != other.technicalName) return false

        return true
    }

    override fun hashCode() = technicalName.hashCode()

    open fun copy(
            technicalName: String = this.technicalName,
            tags: List<ActionTag> = this.tags,
            dependencies: List<ResourceDependency> = this.dependencies,
            name: String = this.name
    ) = ResourceAction(technicalName, tags, dependencies, name)

    override fun toString() = name
}


class ResourceProbabilityAction(
        baseName: String,
        val probability: Double,
        val isWasting: Boolean,
        val canChooseTile: Boolean,
        dependencies: List<ResourceDependency>
) : ResourceAction("_${baseName}_prob_${probability}", listOf(), dependencies, baseName) {
    fun copy(
            baseName: String = this.name,
            probability: Double = this.probability,
            isWasting: Boolean = this.isWasting,
            canChooseTile: Boolean = this.canChooseTile,
            dependencies: List<ResourceDependency> = this.dependencies
    ) = ResourceProbabilityAction(baseName, probability, isWasting, canChooseTile, dependencies)

    override fun copy(
            technicalName: String,
            tags: List<ActionTag>,
            dependencies: List<ResourceDependency>,
            name: String
    ): ResourceAction =  ResourceProbabilityAction(name, probability, isWasting, canChooseTile, dependencies)
}
