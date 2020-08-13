package simulation.culture.group.process.behaviour

import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult

interface GroupBehaviour {
    fun run(group: Group): ProcessResult

    fun update(group: Group): GroupBehaviour? = this

    val internalToString: String
}

abstract class AbstractGroupBehaviour : GroupBehaviour {
    override fun toString() = internalToString
}
