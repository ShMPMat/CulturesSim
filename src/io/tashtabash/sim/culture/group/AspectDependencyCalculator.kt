package io.tashtabash.sim.culture.group

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.AspectPool
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.aspect.dependency.*
import io.tashtabash.sim.culture.group.request.tagEvaluator
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.territory.Territory
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.phony
import java.util.*


class AspectDependencyCalculator(val aspectPool: AspectPool, val territory: Territory) {
    val dependencies: AspectDependencies = AspectDependencies(mutableMapOf())

    fun getAcceptableResources(aspect: Aspect): List<Resource> {
        val allResources = territory.differentResources.toMutableSet()
        allResources.addAll(aspectPool.producedResources)

        return allResources.filter { it.hasApplicationForAction(aspect.core.resourceAction) }
    }

    fun calculateDependencies(aspect: Aspect): AspectDependencies {
        if (aspect is ConverseWrapper) addPhony(aspect)
        addNonPhony(aspect)
        return dependencies
    }

    private fun addPhony(converseWrapper: ConverseWrapper) {
        if (converseWrapper.resource.hasApplicationForAction(converseWrapper.aspect.core.resourceAction)) {
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
                    phony
            )
    }

    private fun addLinePhony(converseWrapper: ConverseWrapper) = addDependenciesInMap(
            aspectPool.converseWrappers
                    .filter { converseWrapper.resource in it.producedResources }
                    .map { LineDependency(true, converseWrapper, it) }
                    .filter { !it.isCycleDependency(converseWrapper) },
            phony
    )

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
