package simulation.culture.group.request

import simulation.space.resource.MutableResourcePack

data class RequestPool(val requests: Map<Request, MutableResourcePack>) {
    fun finish() {
        requests.entries.forEach { it.key.end(it.value) }
    }
}