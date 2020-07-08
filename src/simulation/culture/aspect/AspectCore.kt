package simulation.culture.aspect

import simulation.culture.aspect.complexity.ResourceComplexity
import simulation.culture.aspect.dependency.AspectDependencies
import simulation.space.resource.Resource
import simulation.space.resource.action.ResourceAction
import simulation.space.resource.tag.ResourceTag

data class AspectCore(
        val name: String,
        val tags: List<ResourceTag>,
        val requirements: List<ResourceTag>,
        var applyMeaning: Boolean,
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
