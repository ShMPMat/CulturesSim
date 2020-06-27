package simulation.culture.group.request

import simulation.culture.aspect.Aspect
import simulation.culture.group.centers.Group
import simulation.space.resource.container.MutableResourcePack

class AspectImprovementRequest(
        group: Group,
        private val aspect: Aspect,
        floor: Int,
        ceiling: Int,
        penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        reward: (Pair<Group, MutableResourcePack>, Double) -> Unit
) : Request(group, floor, ceiling, penalty, reward) {
    override fun reducedAmountCopy(amount: Int) =
            AspectImprovementRequest(group, aspect, 0, amount, penalty, reward)

    override val evaluator = aspectEvaluator(aspect)

    override fun reassign(group: Group) =
            AspectImprovementRequest(group, aspect, floor, ceiling, penalty, reward)

    override fun toString() = "want improvement for ${aspect.name}"
}