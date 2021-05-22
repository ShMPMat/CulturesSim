package shmp.simulation.space.resource.transformer

import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.ResourceColour
import shmp.simulation.space.resource.flingConversionLinks


class ColourTransformer(private val colour: ResourceColour) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(appearance = resource.genome.appearance.copy(colour = colour))
        val core = resource.core.copy(genome = genome)
        return Resource(core).flingConversionLinks(resource)
    }
}
