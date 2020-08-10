package simulation.culture.group.process.action.pseudo

import simulation.event.Event


interface GroupPseudoAction {
    fun run(): Any?

    val internalToString: String
}

abstract class AbstractGroupPseudoAction : GroupPseudoAction {
    override fun toString() = internalToString
}

abstract class EventfulGroupPseudoAction : GroupPseudoAction {
    abstract override fun run(): List<Event>

    override fun toString() = internalToString
}
