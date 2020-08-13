package simulation.culture.group.process.interaction

import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult

interface GroupInteraction {
    val initiator: Group
    val participator: Group

    fun run(): ProcessResult
}

abstract class AbstractGroupInteraction(
        override val initiator: Group,
        override val participator: Group
) : GroupInteraction
