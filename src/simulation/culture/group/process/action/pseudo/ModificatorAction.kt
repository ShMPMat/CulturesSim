package simulation.culture.group.process.action.pseudo

import simulation.culture.group.process.interaction.GroupInteraction
import simulation.event.Event


class ActionSequencePA(vararg actions: GroupPseudoAction) : EventfulGroupPseudoAction() {
    private val _actions = actions

    override fun run(): List<Event> = _actions
            .map { it.run() }
            .mapNotNull {
                when (it) {
                    is Event -> listOf(it)
                    is List<*> -> it.filterIsInstance<Event>()
                    else -> null
                }
            }.flatten()

    override val internalToString = "Following happened: " +
            if (_actions.isNotEmpty())
                _actions.joinToString()
            else "nothing"
}

class InteractionWrapperPA(
        val interaction: GroupInteraction,
        override val internalToString: String
) : EventfulGroupPseudoAction() {
    override fun run() = interaction.run()
}
