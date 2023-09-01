package io.tashtabash.sim.space.resource.container

import io.tashtabash.sim.space.resource.Taker


data class ResourcePromisePack(val resources: List<ResourcePromise> = listOf()) {
    constructor(pack: ResourcePack): this(pack.resources.map { ResourcePromise(it) })

    fun extract(taker: Taker) = ResourcePack(resources.map { it.extract(taker) })

    fun makeCopy() = ResourcePack(resources.map { it.makeCopy() })

    fun update() = resources.forEach { it.update() }

    override fun toString() = resources.joinToString()
}
