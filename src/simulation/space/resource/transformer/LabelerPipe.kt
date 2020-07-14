package simulation.space.resource.transformer

import simulation.space.resource.Resource
import simulation.space.resource.tag.labeler.ResourceLabeler

class LabelerPipe(transformer: ResourceTransformer, val labeler: ResourceLabeler) : PipeTransformer(transformer) {
    override fun transform(resource: Resource): Resource {
        val newResource =
                if (labeler.isSuitable(resource.genome))
                    transformer.transform(resource)
                else
                    resource.fullCopy()
        val newParts = newResource.genome.parts.map { transform(it) }
        val genome = newResource.genome.copy(parts = newParts)
        val core = newResource.core.copyCore(genome = genome)
        return Resource(core)
    }
}
