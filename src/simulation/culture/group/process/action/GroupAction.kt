package simulation.culture.group.process.action

import simulation.culture.group.centers.Group

interface GroupAction {
    val group: Group

    fun run(): Any
}

abstract class AbstractGroupAction(override val group: Group) : GroupAction
