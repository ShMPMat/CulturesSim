package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

class PositiveLabeler : ResourceLabeler {
    override fun isSuitable(genome: Genome) = true

    override fun toString() = "Any Resource"
}