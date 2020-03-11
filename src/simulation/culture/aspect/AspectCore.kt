package simulation.culture.aspect

import simulation.culture.aspect.dependency.Dependency
import simulation.culture.group.Group
import simulation.space.resource.tag.ResourceTag

internal class AspectCore(
        val name: String,
        val tags: List<ResourceTag>,
        private val requirements: List<ResourceTag>,
        val matchers: List<AspectMatcher>,
        var applyMeaning: Boolean
) {
    fun getRequirements(): Collection<ResourceTag> {
        return requirements
    }

    fun copy(dependencies: Map<ResourceTag?, Set<Dependency?>?>?, group: Group?): Aspect {
        return Aspect(this, dependencies, group)
    }
}