package simulation.space.resource.transformer

import simulation.space.resource.Resource

interface ResourceTransformer {
    fun transform(resource: Resource): Resource
}