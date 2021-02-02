package shmp.simulation.culture.group.process.behaviour

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.process.ProcessResult

interface GroupBehaviour {
    fun run(group: Group): ProcessResult

    fun update(group: Group): GroupBehaviour? = this

    val internalToString: String
}

abstract class AbstractGroupBehaviour : GroupBehaviour {
    override fun toString() = internalToString
}
