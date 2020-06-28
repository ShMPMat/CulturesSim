package simulation.space.resource.container

data class ResourcePromisePack(val resources: List<ResourcePromise>) {
    fun extract() = ResourcePack(resources.map { it.extract() })

    fun makeCopy() = ResourcePack(resources.map { it.makeCopy() })

    override fun toString() = resources.joinToString()
}
