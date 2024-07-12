package io.tashtabash.sim.culture.aspect.dependency

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.AspectController
import io.tashtabash.sim.culture.aspect.AspectResult
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.group.centers.AspectCenter
import io.tashtabash.sim.culture.group.request.resourceEvaluator
import java.util.*


/**
 * Used only for phony dependencies and applies the aspect to the acquired resource
 */
class LineDependency(
        isPhony: Boolean,
        var converseWrapper: ConverseWrapper,
        private var parentConverseWrapper: ConverseWrapper,
) : AbstractDependency(isPhony) {
    private var isAlreadyUsed = false
    override val name: String
        get() = "from ${converseWrapper.name}"

    override fun isCycleDependency(otherAspect: Aspect): Boolean {
        if (parentConverseWrapper == converseWrapper)
            return true
        if (parentConverseWrapper.isCurrentlyUsed)
            return false

        parentConverseWrapper.isCurrentlyUsed = true
        val result = converseWrapper == otherAspect
        parentConverseWrapper.isCurrentlyUsed = false
        return result
    }

    override fun isCycleDependencyInner(otherAspect: Aspect) = isCycleDependency(otherAspect)

    override fun useDependency(controller: AspectController): AspectResult {
        if (isAlreadyUsed || controller.ceiling <= 0 || !goodForInsertMeaning() && controller.isMeaningNeeded)
            return AspectResult()

        isAlreadyUsed = true
        val result = converseWrapper.use(controller.copy(evaluator = resourceEvaluator(parentConverseWrapper.resource)))
        isAlreadyUsed = false
        return result
    }

    private fun goodForInsertMeaning() = !isPhony || converseWrapper.canInsertMeaning

    override fun copy() = LineDependency(isPhony, converseWrapper, parentConverseWrapper)

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
