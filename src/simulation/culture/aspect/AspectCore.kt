package simulation.culture.aspect

import simulation.culture.aspect.dependency.AspectDependencies
import simulation.culture.aspect.dependency.Dependency
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

    fun copy(dependencies: AspectDependencies): Aspect {
        return Aspect(this, dependencies)
    }
}