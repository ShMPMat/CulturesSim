package io.tashtabash.sim.culture.aspect

import io.tashtabash.sim.culture.aspect.dependency.AspectDependencies
import io.tashtabash.sim.culture.aspect.dependency.LineDependency
import io.tashtabash.sim.culture.group.GroupError
import io.tashtabash.sim.culture.group.centers.AspectCenter
import io.tashtabash.sim.space.resource.ExternalResourceFeature
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.phony
import java.util.*

/**
 * Special Aspect which wraps around another aspect and resource and returns application
 * of this aspect to the resource.
 */
open class ConverseWrapper(var aspect: Aspect, val resource: Resource) : Aspect(
        aspect.core.copy(
                name = aspect.name + "On" + resource.baseName,
                tags = getReducedTags(resource, aspect),
                requirements = ArrayList(aspect.requirements),
                applyMeaning = false,
                standardComplexity = aspect.core.getPrecomputedComplexity(resource)
        ),
        AspectDependencies(mutableMapOf())
) {
    override fun swapDependencies(aspectCenter: AspectCenter) {
        super.swapDependencies(aspectCenter)
        aspect = aspectCenter.aspectPool.getValue(aspect)
    }

    override val producedResources = resource.applyActionUnsafe(aspect.core.resourceAction)

    open fun use(controller: AspectController) = try {
        val result = super._use(controller)
        if (result.isFinished)
            aspect.markAsUsed()

        val targetResources = result.resources
                .getResourcesAndRemove { it in producedResources }
                .toMutableList()
        targetResources.removeIf { it.isEmpty }

        result.resources.addAll(targetResources.map {
            it.copyWithNewExternalFeatures(toFeatures(result.node))
        })

        result
    } catch (e: Exception) {
        throw GroupError("Error in using ConverseWrapper $name: ${e.message}")
    }

    private fun toFeatures(node: ResultNode?): List<ExternalResourceFeature> =
            if (node?.resourceUsed?.isNotEmpty() == true)
                node.resourceUsed.entries
                    .filter { it.key.name != phony.name && !it.key.isInstrumental }
                    .flatMap { p -> p.value.resources.map { it.fullName } }
                    .distinct()
                    .mapIndexed { i, n -> ElementResourceFeature(n, 1000 + i) }
            else emptyList()

    override fun isDependenciesOk(dependencies: AspectDependencies) =
            requirements.size + 1 == dependencies.size

    override val isValid: Boolean
        get() = resource.hasApplicationForAction(aspect.core.resourceAction)

    override fun canTakeResources() =
            aspect.name == "Take" || aspect.name == "Killing" || aspect.name == "TakeApart"

    override fun copy(dependencies: AspectDependencies): ConverseWrapper {
        val copy = ConverseWrapper(
                aspect,
                resource
        )
        copy.initDependencies(dependencies)
        return try {
            copy.canInsertMeaning = dependencies.map.getValue(phony).any {
                it is LineDependency && it.converseWrapper.canInsertMeaning
            }
            copy
        } catch (e: Exception) {
            throw GroupError("Wrong dependencies")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ConverseWrapper

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

private fun getReducedTags(resource: Resource, aspect: Aspect): List<ResourceTag> {
    val result: MutableList<ResourceTag> = ArrayList()
    val allTags = resource.applyActionUnsafe(aspect.core.resourceAction).flatMap { it.tags }
    for (tag in allTags) {
        if (!result.contains(tag))
            result.add(tag)
        else if (result[result.indexOf(tag)].level < tag.level) {
            result.remove(tag)
            result.add(tag)
        }
    }
    return result
}

class ElementResourceFeature(resourceName: String, override val index: Int) : ExternalResourceFeature {
    override val name = "made_with_$resourceName"
}
