package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

class PositiveLabeler : ResourceTagLabeler {
    override fun isSuitable(genome: Genome) = true
}