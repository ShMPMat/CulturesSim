package shmp.simulation.space.resource.container


data class ResourcePromisePack(val resources: List<ResourcePromise> = listOf()) {
    constructor(pack: ResourcePack): this(pack.resources.map { ResourcePromise(it) })

    fun extract() = ResourcePack(resources.map { it.extract() })

    fun makeCopy() = ResourcePack(resources.map { it.makeCopy() })

    fun update() = resources.forEach { it.update() }

    override fun toString() = resources.joinToString()
}
