package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.AspectCenter
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import java.util.*

class ConversionDependency(
        tag: ResourceTag,
        private var aspect: Aspect,
        private val resource: Resource
) : AbstractDependency(tag) {
    override val name: String
        get() = "${aspect.name} on ${resource.baseName}"

    override fun isCycleDependency(otherAspect: Aspect) =
            otherAspect is ConverseWrapper && aspect == otherAspect.aspect

    override fun isCycleDependencyInner(otherAspect: Aspect) =
            isCycleDependency(otherAspect) || otherAspect == otherAspect

    override fun useDependency(controller: AspectController): AspectResult {
        val resourcePack = MutableResourcePack()
        val resourceInstances = controller.territory.getResourceInstances(resource)
        for (res in resourceInstances) {
            if (controller.ceiling <= controller.evaluator.evaluate(resourcePack)) break
            resourcePack.addAll(res.applyAndConsumeAspect(
                    aspect,
                    controller.ceiling - controller.evaluator.evaluate(resourcePack)
            ))
        }
        return AspectResult(resourcePack, null)
    }

    override fun copy() = ConversionDependency(type, aspect, resource)

    override fun swapDependencies(aspectCenter: AspectCenter) {
        aspect =  aspectCenter.aspectPool.get(aspect)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as ConversionDependency
        return super.equals(other) && aspect == that.aspect && resource == that.resource
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), aspect, resource)
    }

}