package simulation.space.resource.transformer

import simulation.space.resource.Resource

class ConcatTransformer(val transformers: List<ResourceTransformer>) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        var transResource = resource

        for (transformer in transformers)
            transResource = transformer.transform(transResource)

        return transResource
    }
}
