package io.tashtabash.sim.culture.group.centers

import io.tashtabash.sim.culture.group.request.RequestType


data class ResourceBan(val allowedTypes: MutableSet<RequestType>, val providers: MutableList<ResourceBanProvider>) {
    override fun toString() = if (allowedTypes.isEmpty()) "" else " only for " + allowedTypes.joinToString()
}


interface ResourceBanProvider {
    val allowedTypes: Set<RequestType>
}
