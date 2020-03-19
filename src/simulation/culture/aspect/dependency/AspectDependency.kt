package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.AspectCenter
import simulation.culture.group.request.ResourceEvaluator
import simulation.space.resource.tag.ResourceTag
import java.util.*

class AspectDependency(tag: ResourceTag, private var aspect: Aspect) : AbstractDependency(tag) {
    override val name: String
        get() = aspect.name

    override fun isCycleDependency(aspect: Aspect) =
            if (aspect is ConverseWrapper && this.aspect == aspect.aspect) {
                true
            } else this.aspect.dependencies.values.any {
                it.any { d -> d.isCycleDependencyInner(aspect) }
            }

    override fun isCycleDependencyInner(aspect: Aspect) =
            if (this.aspect == aspect || aspect is ConverseWrapper && this.aspect == aspect.aspect) {
                true
            } else this.aspect.dependencies.values.any {
                it.any { d -> d.isCycleDependencyInner(aspect) }
            }

    override fun useDependency(controller: AspectController): AspectResult = aspect.use(controller.copy(
            evaluator = ResourceEvaluator(),
            isMeaningNeeded = false
    ))

    override fun copy(): AspectDependency {
        return AspectDependency(type, aspect)
    }

    override fun swapDependencies(aspectCenter: AspectCenter) {
        aspect = aspectCenter.aspectPool.get(aspect)
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