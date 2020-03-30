package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.AspectCenter
import simulation.culture.group.request.zeroingEvaluator
import java.util.*

class AspectDependency(isPhony: Boolean, private var aspect: Aspect) : AbstractDependency(isPhony) {
    override val name: String
        get() = aspect.name

    override fun isCycleDependency(otherAspect: Aspect) =
            if (otherAspect is ConverseWrapper && this.aspect == otherAspect.aspect) true
            else this.aspect.dependencies.map.values.any {
                it.any { d -> d.isCycleDependencyInner(otherAspect) }
            }

    override fun isCycleDependencyInner(otherAspect: Aspect) =
            if (this.aspect == otherAspect || otherAspect is ConverseWrapper && this.aspect == otherAspect.aspect) true
            else this.aspect.dependencies.map.values.any {
                it.any { d -> d.isCycleDependencyInner(otherAspect) }
            }

    override fun useDependency(controller: AspectController): AspectResult = aspect.use(controller.copy(
            evaluator = zeroingEvaluator,//TODO make owner tell what it want
            isMeaningNeeded = false
    ))

    override fun copy() = AspectDependency(isPhony, aspect)

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