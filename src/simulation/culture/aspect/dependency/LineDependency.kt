package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.centers.AspectCenter
import simulation.culture.group.request.resourceEvaluator
import simulation.space.resource.container.MutableResourcePack
import java.util.*

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
        if (parentConverseWrapper.used) return false
        parentConverseWrapper.used = true
        val b = converseWrapper == otherAspect
        parentConverseWrapper.used = false
        return b
    }

    override fun isCycleDependencyInner(otherAspect: Aspect) = isCycleDependency(otherAspect)

    override fun useDependency(controller: AspectController): AspectResult {
        return try {
            val resourcePack = MutableResourcePack()
            if (isAlreadyUsed || controller.ceiling <= 0 || !goodForInsertMeaning() && controller.isMeaningNeeded) {
                return AspectResult(resourcePack, null)
            }
            isAlreadyUsed = true
            val _p = converseWrapper.use(controller.copy(
                    evaluator = resourceEvaluator(parentConverseWrapper.resource)
            ))
            resourcePack.addAll(_p.resources.getResource(parentConverseWrapper.resource).resources.flatMap {
                        it.applyActionAndConsume(
                                parentConverseWrapper.aspect.core.resourceAction,
                                controller.ceiling,
                                true
                        )
                    })
            resourcePack.addAll(_p.resources)
            isAlreadyUsed = false
            AspectResult(_p.isFinished, emptyList(), resourcePack, null)
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