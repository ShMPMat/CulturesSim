package simulation.culture.aspect

import simulation.culture.aspect.dependency.AspectDependencies
import simulation.culture.aspect.dependency.Dependency
import simulation.space.resource.tag.ResourceTag

class AspectCore(
        val name: String,
        val tags: List<ResourceTag>,
        val requirements: List<ResourceTag>,
        val matchers: List<AspectMatcher>,
        var applyMeaning: Boolean,
        val resourceExposed: Boolean,
        val standardComplexity: Double
) {
    fun copy(dependencies: AspectDependencies) = Aspect(this, dependencies)

    val complexity: Double
        get() = standardComplexity
}