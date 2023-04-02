package io.tashtabash.simulation.culture.group.request

import io.tashtabash.simulation.space.resource.container.MutableResourcePack
import io.tashtabash.simulation.space.resource.container.ResourcePack


data class RequestPool(val requests: Map<Request, MutableResourcePack>) {
    val resultStatus: MutableMap<Request, Result> = mutableMapOf()

    fun finish(): ResourcePack {
        resultStatus.putAll(requests.entries.map { (r, p) -> r to r.end(p) })
        return ResourcePack(requests.values.flatMap { it.resources }.map { it.exactCopy() })
    }

    override fun toString() = if (resultStatus.isEmpty()) "No requests were finished"
    else "Finished requests:\n" +
            resultStatus.entries.joinToString("\n") { "${it.key} - ${it.value}" }
}
