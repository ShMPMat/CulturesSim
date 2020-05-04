package simulation.space.resource

class ResourcePool(private val resources: List<ResourceIdeal>) {
    fun get(name: String): Resource {
        for (resource in resources) {
            if (resource.baseName == name) {
                return resource.copy()
            }
        }
        throw NoSuchElementException("No resource with name $name")
    }

    fun getWithPredicate(predicate: (Resource) -> Boolean): List<Resource> = resources
            .filter(predicate)
            .map { it.copy() }

    val all: List<Resource>
        get() = resources
}