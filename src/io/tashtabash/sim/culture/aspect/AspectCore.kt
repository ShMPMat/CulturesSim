package io.tashtabash.sim.culture.aspect

import io.tashtabash.sim.culture.aspect.complexity.ResourceComplexity
import io.tashtabash.sim.culture.aspect.dependency.AspectDependencies
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.action.ActionMatcher
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.tag.ResourceTag


data class AspectCore(
        val name: String,
        val tags: List<ResourceTag>,
        val requirements: List<ResourceTag>,
        val applyMeaning: Boolean,
        val resourceExposed: Boolean,
        val standardComplexity: Double,
        val sideComplexities: List<ResourceComplexity>,
        val matchers: List<ActionMatcher>,
        val resourceAction: ResourceAction
) {
    fun makeAspect(dependencies: AspectDependencies) = Aspect(this, dependencies)

    internal fun getPrecomputedComplexity(resource: Resource) = standardComplexity *
            sideComplexities.map { it.getComplexity(resource) }
                    .foldRight(1.0, Double::times)
}
