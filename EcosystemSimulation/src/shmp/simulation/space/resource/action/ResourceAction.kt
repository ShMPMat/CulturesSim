package shmp.simulation.space.resource.action


open class ResourceAction(val name: String, val matchers: List<ActionMatcher>, val tags: List<ActionTag>)


class ResourceProbabilityAction(
        baseName: String,
        val probability: Double,
        val isWasting: Boolean
) : ResourceAction("_${baseName}_prob_${probability}", listOf(), listOf())
