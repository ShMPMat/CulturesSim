package io.tashtabash.sim.space.resource.transformer

import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.replaceRecursiveLinks


class AddActionTransformer(val action: ResourceAction, val result: List<Resource>) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy()
        genome.conversionCore.addActionConversion(action, result)
        val core = resource.core.copy(genome = genome)
        return Resource(core, resource.amount).replaceRecursiveLinks(resource)
    }
}
