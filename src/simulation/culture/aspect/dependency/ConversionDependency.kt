package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.centers.AspectCenter
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.freeMarker
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

    override fun isCycleDependencyInner(otherAspect: Aspect) = isCycleDependency(otherAspect)

    override fun useDependency(controller: AspectController): AspectResult {
        if (controller.ceiling <= 0)
            return AspectResult(MutableResourcePack(), null)
        val gatheredPack = controller.pickCeilingPart(
                controller.territory.getResourceInstances(resource),
                { it.applyAction(aspect.core.resourceAction) },
                { r, n -> r.getPart(n).applyActionAndConsume(aspect.core.resourceAction, n, false) }
        )
        return AspectResult(gatheredPack, null)
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