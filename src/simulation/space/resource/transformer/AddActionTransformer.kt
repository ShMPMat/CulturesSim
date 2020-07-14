package simulation.space.resource.transformer

import simulation.space.resource.Resource
import simulation.space.resource.action.ResourceAction

class AddActionTransformer(val action: ResourceAction, val result: List<Pair<Resource, Int>>) : ResourceTransformer {
    override fun transform(resource: Resource): Resource {
        val genome = resource.genome.copy()
        genome.conversionCore.addActionConversion(action, result)
        val core = resource.core.copyCore(genome = genome)
        return Resource(core)
    }
}
