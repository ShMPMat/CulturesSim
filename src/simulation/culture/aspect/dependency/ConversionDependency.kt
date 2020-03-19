package simulation.culture.aspect.dependency

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.AspectResult
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.AspectCenter
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import java.util.*

class ConversionDependency(tag: ResourceTag, private var conversion: Pair<Resource, Aspect>) : AbstractDependency(tag) {
    override val name: String
        get() = "${conversion.second.name} on ${conversion.first.baseName}"

    override fun isCycleDependency(aspect: Aspect) =
            aspect is ConverseWrapper && conversion.second == aspect.aspect

    override fun isCycleDependencyInner(aspect: Aspect) =
            isCycleDependency(aspect) || conversion.second == aspect

    override fun useDependency(controller: AspectController): AspectResult {
        val resourcePack = MutableResourcePack()
        val resourceInstances = controller.territory.getResourceInstances(conversion.first)
        for (res in resourceInstances) {
            if (controller.ceiling <= controller.evaluator.evaluate(resourcePack)) break
            resourcePack.addAll(res.applyAndConsumeAspect(
                    conversion.second,
                    controller.ceiling - controller.evaluator.evaluate(resourcePack)
            ))
        }
        return AspectResult(resourcePack, null)
    }

    override fun copy() = ConversionDependency(type, conversion)

    override fun swapDependencies(aspectCenter: AspectCenter) {
        conversion = conversion.copy(second = aspectCenter.aspectPool.get(conversion.second))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as ConversionDependency
        return super.equals(other) && conversion == that.conversion
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), conversion)
    }

}