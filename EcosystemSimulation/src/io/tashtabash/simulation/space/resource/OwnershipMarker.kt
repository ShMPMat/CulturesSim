package io.tashtabash.simulation.space.resource


data class OwnershipMarker(val name: String): Comparable<OwnershipMarker> {
    override fun toString() = name

    override fun compareTo(other: OwnershipMarker) = name.compareTo(other.name)
}


val freeMarker = OwnershipMarker("free")


fun Resource.free() = swapOwnership(freeMarker)

fun Resource.freeCopy() = copyWithOwnership(freeMarker)
