package simulation.culture.group.process.action

import simulation.culture.group.centers.Group

interface GroupAction {
    val group: Group

    fun run(): Any

    val internalToString: String
}

abstract class AbstractGroupAction(override val group: Group) : GroupAction {
    override fun toString() = internalToString
}
