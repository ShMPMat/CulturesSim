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
        tag: ResourceTag,
        private var line: Pair<ConverseWrapper, ConverseWrapper>
) : AbstractDependency(tag) {
    private var isAlreadyUsed = false
    override val name: String
        get() = "${line.first.name} from ${line.second.name}"
    val nextWrapper: ConverseWrapper
        get() = line.second

    override fun isCycleDependency(aspect: Aspect): Boolean {
        if (isAlreadyUsed) return false
        isAlreadyUsed = true
        val b = line.second.dependencies.values.any {
            it.any { d -> d.isCycleDependencyInner(aspect) }
        } || line.second == aspect
        isAlreadyUsed = false
        return b
    }

    override fun isCycleDependencyInner(aspect: Aspect) = isCycleDependency(aspect)

    override fun useDependency(controller: AspectController): AspectResult {
        return try {
            val resourcePack = MutableResourcePack()
            if (isAlreadyUsed || controller.ceiling <= 0 || !goodForInsertMeaning() && controller.isMeaningNeeded) {
                return AspectResult(resourcePack, null)
            }
            isAlreadyUsed = true
            val _p = line.second.use(controller.copy(
                    evaluator = ResourceEvaluator(
                            { it.getPackedResource(line.first.resource) },
                            { it.getAmount(line.first.resource) }
                    )
            ))
            resourcePack.addAll(
                    _p.resources.getPackedResource(line.first.resource).resources
                            .flatMap { it.applyAndConsumeAspect(line.first.aspect, controller.ceiling) }
            )
            resourcePack.addAll(_p.resources)
            isAlreadyUsed = false
            AspectResult(_p.isFinished, resourcePack, null)
        } catch (e: NullPointerException) {
            throw RuntimeException("No such aspect in Group")
        }
    }

    private fun goodForInsertMeaning() = type != ResourceTag.phony() || nextWrapper.canInsertMeaning

    override fun copy() = LineDependency(type, line)

    override fun swapDependencies(aspectCenter: AspectCenter) {
        line = Pair(
                aspectCenter.aspectPool.get(line.second) as ConverseWrapper,
                aspectCenter.aspectPool.get(line.second) as ConverseWrapper
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as LineDependency
        return line == that.line
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), line)
    }

}