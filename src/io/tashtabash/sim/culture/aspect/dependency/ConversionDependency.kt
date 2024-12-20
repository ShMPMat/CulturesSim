package io.tashtabash.sim.culture.aspect.dependency

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.AspectController
import io.tashtabash.sim.culture.aspect.AspectResult
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.group.centers.AspectCenter
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.Taker
import java.util.*

class ConversionDependency(
    isMain: Boolean,
    private var aspect: Aspect,
    private val resource: Resource
) : AbstractDependency(isMain) {
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
                { it.applyActionUnsafe(aspect.core.resourceAction) },
                { r, n ->
                    r.getPart(n, taker).applyActionAndConsume(aspect.core.resourceAction, n, false, taker)
                }
        )
        return AspectResult(MutableResourcePack(gatheredPack))
    }

    override fun copy() = ConversionDependency(isMain, aspect, resource)

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