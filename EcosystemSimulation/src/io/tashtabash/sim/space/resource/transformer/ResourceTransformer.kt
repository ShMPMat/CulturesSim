package io.tashtabash.sim.space.resource.transformer

import io.tashtabash.sim.space.resource.Resource


interface ResourceTransformer {
    fun transform(resource: Resource): Resource
}
