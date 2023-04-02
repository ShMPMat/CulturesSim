package io.tashtabash.simulation.space.resource.transformer

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.ResourceColour
import io.tashtabash.simulation.space.resource.replaceRecursiveLinks


class ColourTransformer(private val colour: ResourceColour) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy(appearance = resource.genome.appearance.copy(colour = colour))
        val core = resource.core.copy(genome = genome)
        return Resource(core, resource.amount).replaceRecursiveLinks(resource)
    }
}
