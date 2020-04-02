package simulation.culture.group.request

import simulation.space.resource.ResourcePack
import java.util.function.Function

/**
 * Class which evaluates ResourcePacks for containing needed Resources.
 */
class ResourceEvaluator(
        private val picker: (ResourcePack) -> ResourcePack,
        private val evaluator: (ResourcePack) -> Int
) {
    fun pick(resourcePack: ResourcePack): ResourcePack {
        return picker(resourcePack)
    }

    fun evaluate(resourcePack: ResourcePack): Int {
        return evaluator(resourcePack)
    }
}