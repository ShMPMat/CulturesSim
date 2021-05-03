package shmp.simulation.space.resource.transformer

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.flingConversionLinks


class AddActionTransformer(val action: ResourceAction, val result: List<Pair<Resource, Int>>) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy()
        genome.conversionCore.addActionConversion(action, result)
        val core = resource.core.copy(genome = genome)
        return Resource(core).flingConversionLinks(resource)
    }
}
