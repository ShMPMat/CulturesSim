package simulation.culture.group

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectPool
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.aspect.dependency.*
import simulation.culture.group.request.tagEvaluator
import simulation.space.Territory
import simulation.space.resource.tag.ResourceTag
import java.util.*

class AspectDependencyCalculator(val aspectPool: AspectPool, val territory: Territory) {
    val dependencies: AspectDependencies = AspectDependencies(mutableMapOf())

    fun calculateDependencies(aspect: Aspect): AspectDependencies {
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
                            true,
                            converseWrapper.aspect,
                            converseWrapper.resource
                    )),
                    ResourceTag.phony()
            )
    }

    private fun addLinePhony(converseWrapper: ConverseWrapper) =
            addDependenciesInMap(aspectPool.producedResources
                    .filter { (f) -> f == converseWrapper.resource }
                    .map { (_, s) -> LineDependency(true, converseWrapper, s) }
                    .filter { !it.isCycleDependency(converseWrapper) },
                    ResourceTag.phony())

    private fun addNonPhony(aspect: Aspect) {
        for (requirement in aspect.requirements) {
            addTagDependencies(requirement, aspect)
        }
    }

    private fun addTagDependencies(requirement: ResourceTag, aspect: Aspect) {
        for (poolAspect in aspectPool.converseWrappers) {
            if (poolAspect.producedResources.any { it.tags.contains(requirement) }) {
                val dependency = AspectDependency(false, poolAspect, tagEvaluator(requirement), aspect)
                if (dependency.isCycleDependency(poolAspect) || dependency.isCycleDependencyInner(aspect)) continue
                addDependenciesInMap(setOf(dependency), requirement)
            }
        }
    }

    private fun addDependenciesInMap(dependencies: Collection<Dependency>, requirement: ResourceTag) {
        if (dependencies.isEmpty()) return
        if (!this.dependencies.map.containsKey(requirement))
            this.dependencies.map[requirement] = HashSet()
        this.dependencies.map.getValue(requirement).addAll(dependencies)
    }
}