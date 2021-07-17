package shmp.simulation.space.resource.transformer

import shmp.simulation.space.resource.Resource


interface ResourceTransformer {
    fun transform(resource: Resource): Resource
}
