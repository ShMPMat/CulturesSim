package simulation.space.resource

data class OwnershipMarker(val name: String) {
    override fun toString() = name
}


val freeMarker = OwnershipMarker("free")


fun free(resource: Resource) = resource.exactCopyAndDestroy(freeMarker)
