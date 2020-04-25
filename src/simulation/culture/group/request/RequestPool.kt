package simulation.culture.group.request

import simulation.space.resource.MutableResourcePack
import simulation.space.resource.ResourcePack

data class RequestPool(val requests: Map<Request, MutableResourcePack>) {
    val resultStatus: MutableMap<Request, Result> = mutableMapOf()

    fun finish(): ResourcePack {
        resultStatus.putAll(requests.entries.map { it.key to it.key.end(it.value) })
        return ResourcePack(requests.values.flatMap { it.resources })
    }

    override fun toString() = if (resultStatus.isEmpty()) "No requests were finished"
    else "Finished requests:\n" +
            resultStatus.entries.joinToString("\n") { "${it.key} - ${it.value}" }
}