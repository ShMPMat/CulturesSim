package shmp.simulation.culture.aspect

import shmp.simulation.culture.aspect.dependency.AspectDependencies
import shmp.simulation.culture.aspect.dependency.LineDependency
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.centers.AspectCenter
import shmp.simulation.space.resource.ExternalResourceFeature
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.phony
import java.util.*

/**
 * Special Aspect which wraps around another aspect and resource and returns application
 * of this aspect to the resource.
 */
open class ConverseWrapper(var aspect: Aspect, resource: Resource) : Aspect(
        aspect.core.copy(
                name = aspect.name + "On" + resource.baseName,
                tags = getReducedTags(resource, aspect),
                requirements = ArrayList(aspect.requirements),
                applyMeaning = false,
                standardComplexity = aspect.core.getPrecomputedComplexity(resource)
        ),
        AspectDependencies(mutableMapOf())
) {
    val resource = resource

    override fun swapDependencies(aspectCenter: AspectCenter) {
        super.swapDependencies(aspectCenter)
        aspect = aspectCenter.aspectPool.getValue(aspect)
    }

    override val producedResources: List<Resource>
        get() = resource.applyAction(aspect.core.resourceAction)

    open fun use(controller: AspectController) = try {
        val result = super._use(controller)
        if (result.isFinished)
            aspect.markAsUsed()

        val targetResources = result.resources
                .getResourcesAndRemove { it in producedResources }
                .resources.toMutableList()
        targetResources.removeIf { it.isEmpty }

        result.resources.addAll(targetResources.map {
            it.copyWithNewExternalFeatures(toFeatures(result.node))
        })

        result
    } catch (e: Exception) {
        throw GroupError("Error in using Converse Wrapper $name")
    }

    private fun toFeatures(node: AspectResult.ResultNode): List<ExternalResourceFeature> =
            if (node.resourceUsed.isNotEmpty())
                node.resourceUsed.entries
                    .filter { it.key.name != phony.name && !it.key.isInstrumental }
                    .flatMap { p -> p.value.resources.map { it.fullName } }
                    .distinct()
                    .mapIndexed { i, n -> ElementResourceFeature(n, 1000 + i) }
            else emptyList()

    override fun isDependenciesOk(dependencies: AspectDependencies) =
            requirements.size + 1 == dependencies.size

    override val isValid: Boolean
        get() {
            if (resource.genome.isResisting && aspect.name == "Take")
                return false
            return resource.hasApplicationForAction(aspect.core.resourceAction)
        }

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
    val allTags = resource.applyAction(aspect.core.resourceAction).flatMap { it.tags }
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
