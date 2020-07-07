package simulation.space.resource

data class OwnershipMarker(val name: String) {
    override fun toString() = name
}

val freeMarker = OwnershipMarker("free")

fun remapOwner(resource: Resource, ownershipMarker: OwnershipMarker) : Resource {
    resource.ownershipMarker = ownershipMarker
    return resource
}

fun free(resource: Resource) = remapOwner(resource, freeMarker)
