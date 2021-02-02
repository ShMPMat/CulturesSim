package shmp.simulation.culture.aspect

import shmp.simulation.culture.aspect.complexity.ResourceComplexity
import shmp.simulation.culture.aspect.dependency.AspectDependencies
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.tag.ResourceTag


data class AspectCore(
        val name: String,
        val tags: List<ResourceTag>,
        val requirements: List<ResourceTag>,
        val applyMeaning: Boolean,
        val resourceExposed: Boolean,
        val standardComplexity: Double,
        val sideComplexities: List<ResourceComplexity>,
        val resourceAction: ResourceAction
) {
    fun makeAspect(dependencies: AspectDependencies) = Aspect(this, dependencies)

    internal fun getPrecomputedComplexity(resource: Resource): Double = standardComplexity *
            sideComplexities.map { it.getComplexity(resource) }
                    .foldRight(1.0, Double::times)
}
