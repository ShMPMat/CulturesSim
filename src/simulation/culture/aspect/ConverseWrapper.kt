package simulation.culture.aspect

import simulation.culture.aspect.dependency.AspectDependencies
import simulation.culture.aspect.dependency.LineDependency
import simulation.culture.group.GroupError
import simulation.culture.group.centers.AspectCenter
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import java.util.*

/**
 * Special Aspect which wraps around another aspect and resource and returns application
 * of this aspect to the resource.
 */
open class ConverseWrapper(var aspect: Aspect, resource: Resource) : Aspect(
        AspectCore(
                aspect.name + "On" + resource.baseName,
                getReducedTags(resource, aspect),
                ArrayList(aspect.requirements),
                ArrayList(),
                false,
                aspect.core.resourceExposed,
                aspect.core.standardComplexity,
                aspect.core.sideComplexities
        ),
        AspectDependencies(mutableMapOf())
) {
    var resource: Resource = resource.copy()

    override fun swapDependencies(aspectCenter: AspectCenter) {
        super.swapDependencies(aspectCenter)
        aspect = aspectCenter.aspectPool.getValue(aspect)
    }

    override val producedResources: List<Resource>
        get() = resource.applyAspect(aspect)

    override fun use(controller: AspectController) = try {
        val result = super.use(controller)
        if (result.isFinished) {
            aspect.markAsUsed()
        }
        result
    } catch (e: Exception) {
        throw GroupError("Error in using Converse Wrapper $name")
    }

    override fun isDependenciesOk(dependencies: AspectDependencies) =
            requirements.size + 1 == dependencies.size

    override val isValid: Boolean
        get() {
            if (resource.genome.isResisting && aspect.name == "Take")
                return false
            return resource.hasApplicationForAspect(aspect)
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
            copy.canInsertMeaning = dependencies.map.getValue(ResourceTag.phony()).any {
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

        if (aspect != other.aspect) return false
        if (resource != other.resource) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + aspect.hashCode()
        result = 31 * result + resource.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

private fun getReducedTags(resource: Resource, aspect: Aspect): List<ResourceTag> {
    val result: MutableList<ResourceTag> = ArrayList()
    val allTags = resource.applyAspect(aspect).flatMap { it.tags }
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