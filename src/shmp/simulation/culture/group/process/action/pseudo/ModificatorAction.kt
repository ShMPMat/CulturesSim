package shmp.simulation.culture.group.process.action.pseudo

import shmp.simulation.culture.group.process.ProcessResult
import shmp.simulation.culture.group.process.flattenPR
import shmp.simulation.culture.group.process.interaction.GroupInteraction
import shmp.simulation.event.Event


class ActionSequencePA(private val actions: List<GroupPseudoAction>) : EventfulGroupPseudoAction() {
    constructor(vararg actions: GroupPseudoAction) : this(actions.toList())

    override fun run(): ProcessResult = actions
            .map { it.run() }
            .mapNotNull {
                when (it) {
                    is Event -> ProcessResult(it)
                    is List<*> -> ProcessResult(events = it.filterIsInstance<Event>())
                    else -> null
                }
            }.flattenPR()

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
