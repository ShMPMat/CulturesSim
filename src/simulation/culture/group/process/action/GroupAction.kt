package simulation.culture.group.process.action

import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.pseudo.GroupPseudoAction


interface GroupAction: GroupPseudoAction {
    val group: Group

    override fun run(): Any?

    override val internalToString: String
}

abstract class AbstractGroupAction(override val group: Group) : GroupAction {
    override fun toString() = internalToString
}
