package io.tashtabash.sim.culture.group.process.action.pseudo

import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.flattenPR
import io.tashtabash.sim.culture.group.process.interaction.GroupInteraction
import io.tashtabash.sim.event.Event


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
            this.actions.joinToString()
                .ifEmpty { "nothing" }
}

class InteractionWrapperPA(
        val interaction: GroupInteraction,
        override val internalToString: String
) : EventfulGroupPseudoAction() {
    override fun run() = interaction.run()
}
