package io.tashtabash.simulation.space.resource.transformer

import io.tashtabash.simulation.space.resource.Resource


class ConcatTransformer(val transformers: List<ResourceTransformer>) : ResourceTransformer {
    constructor(vararg transformers: ResourceTransformer): this(transformers.toList())

    override fun transform(resource: Resource): Resource {
        var transResource = resource

        for (transformer in transformers)
            transResource = transformer.transform(transResource)

        return transResource
    }
}
