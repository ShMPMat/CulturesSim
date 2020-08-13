package simulation.culture.group.process.action.pseudo

import simulation.culture.group.process.ProcessResult


interface GroupPseudoAction {
    fun run(): Any?

    val internalToString: String
}

abstract class AbstractGroupPseudoAction : GroupPseudoAction {
    override fun toString() = internalToString
}

abstract class EventfulGroupPseudoAction : GroupPseudoAction {
    abstract override fun run(): ProcessResult

    override fun toString() = internalToString
}
