package shmp.simulation.culture.group.centers

import shmp.simulation.culture.group.request.RequestPool


class MemoryCenter {
    var turnRequests = RequestPool(mapOf())
        internal set

    fun fullCopy() = MemoryCenter().apply {
        this.turnRequests = turnRequests
    }

    override fun toString() = turnRequests.toString()
}
