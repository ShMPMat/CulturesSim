package simulation.culture.group.process.behaviour

import simulation.event.Event
import simulation.culture.group.centers.Group

interface GroupBehaviour {
    fun run(group: Group): List<Event>

    fun update(group: Group): GroupBehaviour? = this

    val internalToString: String
}

abstract class AbstractGroupBehaviour : GroupBehaviour {
    override fun toString() = internalToString
}
