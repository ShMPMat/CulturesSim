package io.tashtabash.sim.culture.aspect.dependency

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.AspectController
import io.tashtabash.sim.culture.aspect.AspectResult
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.group.centers.AspectCenter
import io.tashtabash.sim.culture.group.request.resourceEvaluator
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import java.util.*
import kotlin.math.ceil


class LineDependency(
        isPhony: Boolean,
        private var parentConverseWrapper: ConverseWrapper,
        var converseWrapper: ConverseWrapper
) : AbstractDependency(isPhony) {
    private var isAlreadyUsed = false
    override val name: String
        get() = "from ${converseWrapper.name}"

    override fun isCycleDependency(otherAspect: Aspect): Boolean {
        if (parentConverseWrapper == converseWrapper) return true
        if (parentConverseWrapper.isCurrentlyUsed) return false
        parentConverseWrapper.isCurrentlyUsed = true
        val b = converseWrapper == otherAspect
        parentConverseWrapper.isCurrentlyUsed = false
        return b
    }

    override fun isCycleDependencyInner(otherAspect: Aspect) = isCycleDependency(otherAspect)

    override fun useDependency(controller: AspectController): AspectResult {
        return try {
            val resourcePack = MutableResourcePack()
            if (isAlreadyUsed || controller.ceiling <= 0 || !goodForInsertMeaning() && controller.isMeaningNeeded)
                return AspectResult(resourcePack)

            isAlreadyUsed = true
            val _p = converseWrapper.use(controller.copy(
                    evaluator = resourceEvaluator(parentConverseWrapper.resource)
            ))
            resourcePack.addAll(_p.resources.getResource(parentConverseWrapper.resource).resources.flatMap {
                        it.applyActionAndConsume(
                                parentConverseWrapper.aspect.core.resourceAction,
                                ceil(controller.ceiling).toInt(),
                                true,
                                Taker.ResourceTaker(controller.populationCenter.actualPopulation)
                        )
                    })
            resourcePack.addAll(_p.resources)
            isAlreadyUsed = false
            AspectResult(resourcePack, null, _p.isFinished)
        } catch (e: NullPointerException) {
            isAlreadyUsed = false
            throw RuntimeException("No such aspect in Group")
        }
    }

    private fun goodForInsertMeaning() = !isPhony || converseWrapper.canInsertMeaning

    override fun copy() = LineDependency(isPhony, parentConverseWrapper, converseWrapper)

    override fun swapDependencies(aspectCenter: AspectCenter) {
        parentConverseWrapper = aspectCenter.aspectPool.getValue(parentConverseWrapper) as ConverseWrapper
        converseWrapper = aspectCenter.aspectPool.getValue(converseWrapper) as ConverseWrapper
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as LineDependency
        return parentConverseWrapper == that.parentConverseWrapper
                && converseWrapper == that.converseWrapper
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), parentConverseWrapper, converseWrapper)
    }
}
