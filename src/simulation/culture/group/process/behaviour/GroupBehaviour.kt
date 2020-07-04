package simulation.culture.group.process.behaviour

import simulation.Event
import simulation.culture.group.centers.Group

interface GroupBehaviour {
    fun run(group: Group): List<Event>

    fun update(group: Group): GroupBehaviour = this
}

abstract class AbstractGroupBehaviour : GroupBehaviour
