package io.tashtabash.simulation.space.resource.transformer

import io.tashtabash.simulation.space.resource.Resource


interface ResourceTransformer {
    fun transform(resource: Resource): Resource
}
