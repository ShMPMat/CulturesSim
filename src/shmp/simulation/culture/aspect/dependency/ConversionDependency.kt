package shmp.simulation.culture.aspect.dependency

import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.AspectController
import shmp.simulation.culture.aspect.AspectResult
import shmp.simulation.culture.aspect.ConverseWrapper
import shmp.simulation.culture.group.centers.AspectCenter
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.Taker
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
            return AspectResult()

        val taker = Taker.ResourceTaker(controller.populationCenter.actualPopulation)

        val gatheredPack = controller.pickCeilingPart(
                controller.territory.getResourceInstances(resource),
                { it.applyAction(aspect.core.resourceAction) },
                { r, n ->
                    r.getPart(n, taker).applyActionAndConsume(aspect.core.resourceAction, n, false, taker)
                }
        )
        return AspectResult(gatheredPack)
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