package simulation.culture.group.request

import simulation.culture.aspect.Aspect
import simulation.culture.group.centers.Group

class AspectImprovementRequest(val aspect: Aspect, core: RequestCore) : Request(core) {
    override fun reducedAmountCopy(amount: Double) =
            AspectImprovementRequest(aspect, core.copy(floor = 0.0, ceiling = amount))

    override val evaluator = aspectEvaluator(aspect)

    override fun reassign(group: Group) =
            AspectImprovementRequest(aspect, core.copy(group = group))

    override fun toString() = "want improvement for ${aspect.name}"
}
