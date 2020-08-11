package simulation.culture.group.process.action.pseudo

import simulation.culture.group.process.interaction.GroupInteraction
import simulation.event.Event


class ActionSequencePA(private val actions: List<GroupPseudoAction>) : EventfulGroupPseudoAction() {
    constructor(vararg actions: GroupPseudoAction) : this(actions.toList())

    override fun run(): List<Event> = actions
            .map { it.run() }
            .mapNotNull {
                when (it) {
                    is Event -> listOf(it)
                    is List<*> -> it.filterIsInstance<Event>()
                    else -> null
                }
            }.flatten()

    override val internalToString = "Following happened: " +
            if (this.actions.isNotEmpty())
                this.actions.joinToString()
            else "nothing"
}

class InteractionWrapperPA(
        val interaction: GroupInteraction,
        override val internalToString: String
) : EventfulGroupPseudoAction() {
    override fun run() = interaction.run()
}
