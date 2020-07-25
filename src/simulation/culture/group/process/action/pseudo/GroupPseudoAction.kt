package simulation.culture.group.process.action.pseudo


interface GroupPseudoAction {
    fun run(): Any

    val internalToString: String
}

abstract class AbstractGroupPseudoAction : GroupPseudoAction {
    override fun toString() = internalToString
}
