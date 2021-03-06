package shmp.simulation.culture.aspect.dependency

import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.AspectController
import shmp.simulation.culture.aspect.AspectResult
import shmp.simulation.culture.aspect.ConverseWrapper
import shmp.simulation.culture.group.centers.AspectCenter
import shmp.simulation.culture.group.request.ResourceEvaluator
import shmp.simulation.space.resource.container.MutableResourcePack
import java.util.*


class AspectDependency(//TODO what's the difference between this and LineDependency?
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
        if (parentAspect.used)
            return false
        parentAspect.used = true
        val result = otherAspect is ConverseWrapper && this.aspect == otherAspect.aspect
        parentAspect.used = false
        return result
    }

    override fun isCycleDependencyInner(otherAspect: Aspect) =
            if (this.aspect == otherAspect) true
            else isCycleDependency(otherAspect)

    override fun useDependency(controller: AspectController): AspectResult {
        if (isAlreadyUsed || controller.ceiling <= 0)//TODO mb unnecessary
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
