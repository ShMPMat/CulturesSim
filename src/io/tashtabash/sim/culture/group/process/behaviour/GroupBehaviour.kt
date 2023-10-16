package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult


interface GroupBehaviour {
    fun run(group: Group): ProcessResult

    fun update(group: Group): GroupBehaviour? = this

    val internalToString: String
}

abstract class AbstractGroupBehaviour : GroupBehaviour {
    override fun toString() = internalToString
}
