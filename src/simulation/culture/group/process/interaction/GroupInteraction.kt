package simulation.culture.group.process.interaction

import simulation.event.Event
import simulation.culture.group.centers.Group

interface GroupInteraction {
    val initiator: Group
    val participator: Group

    fun run(): List<Event>
}

abstract class AbstractGroupInteraction(
        override val initiator: Group,
        override val participator: Group
) : GroupInteraction
