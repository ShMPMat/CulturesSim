package shmp.simulation.space.resource


data class OwnershipMarker(val name: String): Comparable<OwnershipMarker> {
    override fun toString() = name

    override fun compareTo(other: OwnershipMarker) = name.compareTo(other.name)
}


val freeMarker = OwnershipMarker("free")


fun free(resource: Resource) = resource.exactCopyAndDestroy(freeMarker)

fun freeCopy(resource: Resource) = resource.exactCopy(freeMarker)
