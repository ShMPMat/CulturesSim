package io.tashtabash.sim.culture.group

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.AspectPool
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.aspect.dependency.*
import io.tashtabash.sim.culture.group.centers.ResourceNeed
import io.tashtabash.sim.culture.group.request.tagEvaluator
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.territory.Territory
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.labeler.BaseNameLabeler
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler
import io.tashtabash.sim.space.resource.tag.labeler.TagLabeler
import io.tashtabash.sim.space.resource.tag.phony
import java.util.*


class AspectDependencyCalculator(
    val aspectPool: AspectPool,
    val territory: Territory,
    val secondaryAspectPool: AspectPool?
) {
    val dependencies: AspectDependencies = AspectDependencies(mutableMapOf())
    val needs: MutableMap<ResourceLabeler, Pair<ResourceNeed, ResourceTag>> = mutableMapOf()

    private val availableConverseWrappers: Set<ConverseWrapper>
        get() = aspectPool.converseWrappers +
                (secondaryAspectPool?.converseWrappers ?: emptyList())

    fun getAcceptableResources(aspect: Aspect): List<Resource> {
        val allResources = territory.differentResources.toMutableSet()
        allResources += aspectPool.producedResources
        if (secondaryAspectPool != null)
            allResources += secondaryAspectPool.producedResources

        return allResources.filter { it.hasApplicationForAction(aspect.core.resourceAction) }
    }

    fun calculateDependencies(aspect: Aspect): AspectDependencies {
        if (aspect is ConverseWrapper)
            addPhony(aspect)
        addNonPhony(aspect)
        return dependencies
    }

    private fun addPhony(converseWrapper: ConverseWrapper) {
        if (converseWrapper.resource.hasApplicationForAction(converseWrapper.aspect.core.resourceAction)) {
            addTakeablePhony(converseWrapper)
            addLinePhony(converseWrapper)

            if (dependencies.safePhony == null)
                addNeed(BaseNameLabeler(converseWrapper.resource.baseName), phony)
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
        availableConverseWrappers.filter { converseWrapper.resource in it.producedResources }
                    .map { LineDependency(true, it, converseWrapper) }
                    .filter { !it.isCycleDependency(converseWrapper) },
            phony
    )

    private fun addNonPhony(aspect: Aspect) {
        for (requirement in aspect.requirements)
            addTagDependencies(requirement, aspect)
    }

    private fun addTagDependencies(requirement: ResourceTag, aspect: Aspect) {
        val dependencies = mutableSetOf<Dependency>()

        for (converseWrapper in availableConverseWrappers) {
            if (converseWrapper.producedResources.any { it.tags.contains(requirement) }) {
                val dependency = AspectDependency(false, converseWrapper, tagEvaluator(requirement), aspect)
                if (dependency.isCycleDependency(converseWrapper) || dependency.isCycleDependencyInner(aspect))
                    continue
                dependencies += dependency
            }
        }

        addDependenciesInMap(dependencies, requirement)

        if (dependencies.isEmpty())
            addNeed(TagLabeler(requirement), requirement)
    }

    private fun addNeed(resourceLabeler: ResourceLabeler, resourceTag: ResourceTag) {
        needs[resourceLabeler] = ResourceNeed(1, resourceLabeler) to resourceTag
    }

    private fun addDependenciesInMap(dependencies: Collection<Dependency>, requirement: ResourceTag) {
        if (dependencies.isEmpty()) return
        if (!this.dependencies.map.containsKey(requirement))
            this.dependencies.map[requirement] = HashSet()
        this.dependencies.map.getValue(requirement) += dependencies
    }
}
