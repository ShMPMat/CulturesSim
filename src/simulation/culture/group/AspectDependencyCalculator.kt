package simulation.culture.group

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectPool
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.aspect.dependency.AspectDependency
import simulation.culture.aspect.dependency.ConversionDependency
import simulation.culture.aspect.dependency.Dependency
import simulation.culture.aspect.dependency.LineDependency
import simulation.space.Territory
import simulation.space.resource.tag.ResourceTag
import java.util.*

class AspectDependencyCalculator(val aspectPool: AspectPool, val territory: Territory) {
    val dependencies: MutableMap<ResourceTag, MutableSet<Dependency>> = mutableMapOf()

    fun calculateDependencies(aspect: Aspect): Map<ResourceTag, Set<Dependency>> {
        if (aspect is ConverseWrapper) addPhony(aspect)
        addNonPhony(aspect)
        return dependencies
    }

    private fun addPhony(converseWrapper: ConverseWrapper) {
        if (converseWrapper.resource.hasApplicationForAspect(converseWrapper.aspect)) {
            addTakeablePhony(converseWrapper)
            addLinePhony(converseWrapper)
        }
    }

    private fun addTakeablePhony(converseWrapper: ConverseWrapper) {
        if (converseWrapper.canTakeResources() && territory.differentResources.contains(converseWrapper.resource))
            addDependenciesInMap(
                    setOf(ConversionDependency(
                            converseWrapper.requirement,
                            Pair(converseWrapper.resource, converseWrapper.aspect)
                    )),
                    converseWrapper.requirement
            )
    }

    private fun addLinePhony(converseWrapper: ConverseWrapper) =
            addDependenciesInMap(aspectPool.producedResources
                    .filter { (f) -> f == converseWrapper.resource }
                    .map { (_, s) ->
                        LineDependency(
                                converseWrapper.requirement,
                                Pair(converseWrapper, s)
                        )
                    }.filter { !it.isCycleDependency(converseWrapper) },
                    converseWrapper.requirement)

    private fun addNonPhony(aspect: Aspect) {
        for (requirement in aspect.requirements) {
            addTagDependencies(requirement, aspect)
        }
    }

    private fun addTagDependencies(requirement: ResourceTag, aspect: Aspect) {
        for (poolAspect in aspectPool.getAll()) {
            if (poolAspect.tags.contains(requirement)) {
                val dependency = AspectDependency(requirement, poolAspect)
                if (dependency.isCycleDependency(poolAspect) || dependency.isCycleDependencyInner(aspect)) continue
                addDependenciesInMap(setOf(dependency), requirement)
            }
            addDependenciesInMap(
                    territory.getResourcesWhichConverseToTag(poolAspect, requirement)
                            .map { ConversionDependency(requirement, Pair(it, poolAspect)) }//Make converse Dependency_
                            .filter { !it.isCycleDependency(aspect) },
                    requirement
            )
        }
    }

    private fun addDependenciesInMap(dependencies: Collection<Dependency>, requirement: ResourceTag) {
        if (dependencies.isEmpty()) return
        if (!this.dependencies.containsKey(requirement))
            this.dependencies[requirement] = HashSet()
        this.dependencies.getValue(requirement).addAll(dependencies)
    }
}