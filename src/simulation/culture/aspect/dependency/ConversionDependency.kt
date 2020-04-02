package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.AspectCenter
import simulation.space.resource.Resource
import java.util.*

class ConversionDependency(
        isPhony: Boolean,
        private var aspect: Aspect,
        private val resource: Resource
) : AbstractDependency(isPhony) {
    override val name: String
        get() = "${aspect.name} on ${resource.baseName}"

    override fun isCycleDependency(otherAspect: Aspect) =
            otherAspect is ConverseWrapper && aspect == otherAspect.aspect

    override fun isCycleDependencyInner(otherAspect: Aspect) =
            isCycleDependency(otherAspect) || otherAspect == aspect

    override fun useDependency(controller: AspectController): AspectResult {
        val resourceInstances = controller.territory.getResourceInstances(resource)
        val resourcePack = controller.pickCeilingPart(
                resourceInstances,
                { it.applyAspect(aspect) },
                { r, n -> r.getPart(n).applyAndConsumeAspect(aspect, n) }
        )
        return AspectResult(resourcePack, null)
    }

    override fun copy() = ConversionDependency(isPhony, aspect, resource)

    override fun swapDependencies(aspectCenter: AspectCenter) {
        aspect =  aspectCenter.aspectPool.getValue(aspect)
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