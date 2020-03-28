package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.AspectCenter
import simulation.culture.group.request.ResourceEvaluator
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.tag.ResourceTag
import java.util.*

class LineDependency(
        isPhony: Boolean,
        private var parentConverseWrapper: ConverseWrapper,
        var converseWrapper: ConverseWrapper
) : AbstractDependency(isPhony) {
    private var isAlreadyUsed = false
    override val name: String
        get() = "${parentConverseWrapper.name} from ${converseWrapper.name}"

    override fun isCycleDependency(otherAspect: Aspect): Boolean {
        if (isAlreadyUsed) return false
        isAlreadyUsed = true
        val b = converseWrapper.dependencies.map.values.any {
            it.any { d -> d.isCycleDependencyInner(otherAspect) }
        } || converseWrapper == otherAspect
        isAlreadyUsed = false
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
                    evaluator = ResourceEvaluator(
                            { it.getPackedResource(parentConverseWrapper.resource) },
                            { it.getAmount(parentConverseWrapper.resource) }
                    )
            ))
            resourcePack.addAll(
                    _p.resources.getPackedResource(parentConverseWrapper.resource).resources
                            .flatMap { it.applyAndConsumeAspect(parentConverseWrapper.aspect, controller.ceiling) }
            )
            resourcePack.addAll(_p.resources)
            isAlreadyUsed = false
            AspectResult(_p.isFinished, resourcePack, null)
        } catch (e: NullPointerException) {
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