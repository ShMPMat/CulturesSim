package simulation.space.resource

class ResourcePool(private val resources: List<ResourceIdeal>) {
    fun getResource(name: String): Resource {
        for (resource in resources) {
            if (resource.baseName == name) {
                return resource.copy()
            }
        }
        throw NoSuchElementException("No resource with name $name")
    }

    fun getResourcesWithPredicate(predicate: (Resource) -> Boolean): List<Resource> = resources
            .filter(predicate)
            .map { it.copy() }

    fun getResources(): List<Resource> = resources
}