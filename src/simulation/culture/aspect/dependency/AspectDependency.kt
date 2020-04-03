package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.AspectCenter
import simulation.culture.group.request.ResourceEvaluator
import simulation.space.resource.MutableResourcePack
import java.util.*

class AspectDependency(
        isPhony: Boolean,
        private var aspect: Aspect,
        private val evaluator: ResourceEvaluator,
        private val parentAspect: Aspect
) : AbstractDependency(isPhony) {
    private var isAlreadyUsed = false
    override val name: String
        get() = aspect.name

    override fun isCycleDependency(otherAspect: Aspect): Boolean {
        if (parentAspect.used)
            return false
        parentAspect.used = true
        val result = if (otherAspect is ConverseWrapper && this.aspect == otherAspect.aspect) true
        else this.aspect.dependencies.map.values.any {
            it.any { d -> d.isCycleDependencyInner(otherAspect) }
        }
        parentAspect.used = false
        return result
    }

    override fun isCycleDependencyInner(otherAspect: Aspect) =
            if (this.aspect == otherAspect) true
            else isCycleDependency(otherAspect)

    override fun useDependency(controller: AspectController): AspectResult {
        if (isAlreadyUsed || controller.ceiling <= 0)
            return AspectResult(MutableResourcePack(), null)
        isAlreadyUsed = true
        val result = aspect.use(controller.copy(
                evaluator = evaluator,
                isMeaningNeeded = false
        ))
        isAlreadyUsed = false
        return result
    }

    override fun copy() = AspectDependency(isPhony, aspect, evaluator, parentAspect)

    override fun swapDependencies(aspectCenter: AspectCenter) {
        aspect = aspectCenter.aspectPool.getValue(aspect)
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