package io.tashtabash.sim.culture.aspect.dependency

import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.AspectController
import io.tashtabash.sim.culture.aspect.AspectResult
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.group.centers.AspectCenter
import io.tashtabash.sim.culture.group.request.ResourceEvaluator
import java.util.*


class AspectDependency(
        isPhony: Boolean,
        aspect: ConverseWrapper,
        private val evaluator: ResourceEvaluator,
        private val parentAspect: Aspect
) : AbstractDependency(isPhony) {
    var aspect = aspect
        private set
    private var isAlreadyUsed = false
    override val name: String
        get() = aspect.name

    override fun isCycleDependency(otherAspect: Aspect): Boolean {
        if (parentAspect.isCurrentlyUsed)
            return false
        if (aspect == parentAspect)
            return true

        parentAspect.isCurrentlyUsed = true
        val result = otherAspect is ConverseWrapper && this.aspect == otherAspect.aspect
        parentAspect.isCurrentlyUsed = false
        return result
    }

    override fun isCycleDependencyInner(otherAspect: Aspect) =
            if (this.aspect == otherAspect) true
            else isCycleDependency(otherAspect)

    override fun useDependency(controller: AspectController): AspectResult {
        if (isAlreadyUsed || controller.ceiling <= 0)
            return AspectResult()

        isAlreadyUsed = true
        val result = aspect.use(controller.copy(evaluator = evaluator, isMeaningNeeded = false))
        isAlreadyUsed = false
        return result
    }

    override fun copy() = AspectDependency(isPhony, aspect, evaluator, parentAspect)

    override fun swapDependencies(aspectCenter: AspectCenter) {
        aspect = aspectCenter.aspectPool.getValue(aspect) as ConverseWrapper
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as AspectDependency
        return aspect == that.aspect
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), aspect)
    }
}
