package shmp.simulation.space.resource.action


open class ResourceAction(val name: String, val matchers: List<ActionMatcher>, val tags: List<ActionTag>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResourceAction) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode() = name.hashCode()
}


class ResourceProbabilityAction(
        baseName: String,
        val probability: Double,
        val isWasting: Boolean
) : ResourceAction("_${baseName}_prob_${probability}", listOf(), listOf())
