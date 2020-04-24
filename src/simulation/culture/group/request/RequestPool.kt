package simulation.culture.group.request

import simulation.space.resource.MutableResourcePack
import simulation.space.resource.ResourcePack

data class RequestPool(val requests: Map<Request, MutableResourcePack>) {
    fun finish(): ResourcePack {
        requests.entries.forEach { it.key.end(it.value) }
        return ResourcePack(requests.values.flatMap { it.resources })
    }
}