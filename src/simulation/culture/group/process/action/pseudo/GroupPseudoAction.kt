package simulation.culture.group.process.action.pseudo

import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.GroupAction


interface GroupPseudoAction {
    fun run(): Any

    val internalToString: String
}

abstract class AbstractGroupPseudoAction(override val group: Group) : GroupAction {
    override fun toString() = internalToString
}
