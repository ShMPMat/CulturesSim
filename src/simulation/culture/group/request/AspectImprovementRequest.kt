package simulation.culture.group.request

import simulation.culture.aspect.Aspect
import simulation.culture.group.centers.Group
import simulation.space.resource.container.MutableResourcePack

class AspectImprovementRequest(
        group: Group,
        private val aspect: Aspect,
        floor: Double,
        ceiling: Double,
        penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        reward: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        need: Int
) : Request(group, floor, ceiling, penalty, reward, need) {
    override fun reducedAmountCopy(amount: Double): Request =
            AspectImprovementRequest(group, aspect, 0.0, amount, penalty, reward, need)

    override val evaluator = aspectEvaluator(aspect)

    override fun reassign(group: Group) =
            AspectImprovementRequest(group, aspect, floor, ceiling, penalty, reward, need)

    override fun toString() = "want improvement for ${aspect.name}"
}